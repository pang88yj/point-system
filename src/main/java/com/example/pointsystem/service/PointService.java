package com.example.pointsystem.service;

import com.example.pointsystem.domain.*;
import com.example.pointsystem.dto.*;
import com.example.pointsystem.exception.BusinessException;
import com.example.pointsystem.exception.ErrorCode;
import com.example.pointsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final UserRepository userRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointBucketRepository pointBucketRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointUsageAllocationRepository pointUsageAllocationRepository;

    @Transactional
    public EarnPointResponse earn(EarnPointRequest request) {
        validatePositiveAmount(request.getAmount());

        PointPolicy policy = getPolicy();
        User user = getOrCreateUser(request.getUserId());

        int expireDays = request.getExpireDays() != null ? request.getExpireDays() : policy.getDefaultExpireDays();
        validateExpireDays(expireDays);

        if (request.getAmount() > policy.getMaxEarnPerTxn()) {
            throw new BusinessException(ErrorCode.POLICY_VIOLATION);
        }

        int currentBalance = calculateAvailableBalance(user);
        if (currentBalance + request.getAmount() > policy.getMaxBalancePerUser()) {
            throw new BusinessException(ErrorCode.POLICY_VIOLATION);
        }

        String pointKey = generatePointKey();
        PointLedger ledger = PointLedger.create(
                pointKey,
                user,
                PointLedgerType.EARN,
                request.getAmount(),
                null,
                null,
                request.getDescription()
        );
        pointLedgerRepository.save(ledger);

        PointSourceType sourceType = request.isManual() ? PointSourceType.MANUAL : PointSourceType.NORMAL;

        PointBucket bucket = PointBucket.create(
                pointKey,
                user,
                request.getAmount(),
                LocalDateTime.now().plusDays(expireDays),
                sourceType,
                ledger
        );
        pointBucketRepository.save(bucket);

        return new EarnPointResponse(
                pointKey,
                user.getUserId(),
                bucket.getOriginalAmount(),
                bucket.getRemainingAmount(),
                bucket.getExpireAt()
        );
    }

    @Transactional
    public void cancelEarn(CancelEarnRequest request) {
        User user = getUser(request.getUserId());
        PointBucket bucket = pointBucketRepository.findByPointKey(request.getPointKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

        validateOwnership(user, bucket.getUser());

        if (!bucket.isFullyUnused()) {
            throw new BusinessException(ErrorCode.ALREADY_USED_POINT);
        }

        PointLedger cancelLedger = PointLedger.create(
                generatePointKey(),
                user,
                PointLedgerType.EARN_CANCEL,
                bucket.getOriginalAmount(),
                null,
                bucket.getPointKey(),
                "earn cancel"
        );
        pointLedgerRepository.save(cancelLedger);

        bucket.cancel();
    }

    @Transactional
    public UsePointResponse use(UsePointRequest request) {
        validatePositiveAmount(request.getAmount());

        User user = getUser(request.getUserId());
        LocalDateTime now = LocalDateTime.now();

        List<PointBucket> allBuckets = pointBucketRepository.findByUser(user);

        allBuckets.forEach(bucket -> bucket.expireIfNeeded(now));

        List<PointBucket> usableBuckets = allBuckets.stream()
                .filter(bucket -> bucket.getRemainingAmount() > 0)
                .filter(bucket -> !bucket.isExpired(now))
                .filter(bucket -> bucket.getStatus() != PointBucketStatus.CANCELLED)
                .sorted(
                        Comparator
                                .comparing((PointBucket b) -> b.getSourceType() != PointSourceType.MANUAL)
                                .thenComparing(PointBucket::getExpireAt)
                                .thenComparing(PointBucket::getCreatedAt)
                )
                .toList();

        int totalUsable = usableBuckets.stream()
                .mapToInt(PointBucket::getRemainingAmount)
                .sum();

        if (totalUsable < request.getAmount()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        String usePointKey = generatePointKey();
        PointLedger useLedger = PointLedger.create(
                usePointKey,
                user,
                PointLedgerType.USE,
                request.getAmount(),
                request.getOrderNo(),
                null,
                "point use"
        );
        pointLedgerRepository.save(useLedger);

        int remainingToUse = request.getAmount();
        List<UseAllocationDto> allocationDtos = new ArrayList<>();

        for (PointBucket bucket : usableBuckets) {
            if (remainingToUse == 0) {
                break;
            }

            int useAmount = Math.min(bucket.getRemainingAmount(), remainingToUse);
            bucket.deduct(useAmount);

            PointUsageAllocation allocation = PointUsageAllocation.create(useLedger, bucket, useAmount);
            pointUsageAllocationRepository.save(allocation);

            allocationDtos.add(new UseAllocationDto(bucket.getPointKey(), useAmount));
            remainingToUse -= useAmount;
        }

        return new UsePointResponse(
                usePointKey,
                request.getOrderNo(),
                request.getAmount(),
                allocationDtos
        );
    }

    @Transactional
    public CancelUseResponse cancelUse(CancelUseRequest request) {
        validatePositiveAmount(request.getCancelAmount());

        User user = getUser(request.getUserId());
        PointLedger useLedger = pointLedgerRepository.findByPointKey(request.getPointKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

        if (useLedger.getType() != PointLedgerType.USE) {
            throw new BusinessException(ErrorCode.INVALID_POINT_TYPE);
        }

        validateOwnership(user, useLedger.getUser());

        List<PointUsageAllocation> allocations =
                pointUsageAllocationRepository.findByUseLedgerOrderByIdAsc(useLedger);

        int cancelableAmount = allocations.stream()
                .mapToInt(PointUsageAllocation::getCancelableAmount)
                .sum();

        if (request.getCancelAmount() > cancelableAmount) {
            throw new BusinessException(ErrorCode.INVALID_CANCEL_AMOUNT);
        }

        String cancelPointKey = generatePointKey();
        PointLedger cancelLedger = PointLedger.create(
                cancelPointKey,
                user,
                PointLedgerType.USE_CANCEL,
                request.getCancelAmount(),
                useLedger.getOrderNo(),
                useLedger.getPointKey(),
                "use cancel"
        );
        pointLedgerRepository.save(cancelLedger);

        int remainingToCancel = request.getCancelAmount();
        List<CancelRestoreDto> restoreDtos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (PointUsageAllocation allocation : allocations) {
            if (remainingToCancel == 0) {
                break;
            }

            int ableToCancel = allocation.getCancelableAmount();
            if (ableToCancel == 0) {
                continue;
            }

            int restoreAmount = Math.min(ableToCancel, remainingToCancel);
            PointBucket sourceBucket = allocation.getSourceBucket();

            if (sourceBucket.isExpired(now)) {
                String restoredPointKey = generatePointKey();

                PointLedger restoredLedger = PointLedger.create(
                        restoredPointKey,
                        user,
                        PointLedgerType.RESTORE_EARN,
                        restoreAmount,
                        null,
                        sourceBucket.getPointKey(),
                        "restore expired point as new earn"
                );
                pointLedgerRepository.save(restoredLedger);

                PointBucket restoredBucket = PointBucket.create(
                        restoredPointKey,
                        user,
                        restoreAmount,
                        now.plusDays(getPolicy().getDefaultExpireDays()),
                        PointSourceType.RESTORED,
                        restoredLedger
                );
                pointBucketRepository.save(restoredBucket);

                restoreDtos.add(new CancelRestoreDto(restoredPointKey, restoreAmount, "NEW_EARN_FOR_EXPIRED"));
            } else {
                sourceBucket.restore(restoreAmount);
                restoreDtos.add(new CancelRestoreDto(sourceBucket.getPointKey(), restoreAmount, "RESTORE_BUCKET"));
            }

            allocation.cancel(restoreAmount);
            remainingToCancel -= restoreAmount;
        }

        return new CancelUseResponse(cancelPointKey, request.getCancelAmount(), restoreDtos);
    }

    public BalanceResponse getBalance(String userId) {
        User user = getUser(userId);
        int balance = calculateAvailableBalance(user);
        return new BalanceResponse(userId, balance);
    }

    private int calculateAvailableBalance(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<PointBucket> buckets = pointBucketRepository.findByUser(user);

        buckets.forEach(bucket -> bucket.expireIfNeeded(now));

        return buckets.stream()
                .filter(bucket -> bucket.getStatus() != PointBucketStatus.CANCELLED)
                .filter(bucket -> !bucket.isExpired(now))
                .mapToInt(PointBucket::getRemainingAmount)
                .sum();
    }

    private User getOrCreateUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseGet(() -> userRepository.save(User.create(userId)));
    }

    private User getUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private PointPolicy getPolicy() {
        return pointPolicyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
    }

    private void validatePositiveAmount(int amount) {
        if (amount < 1) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }
    }

    private void validateExpireDays(int expireDays) {
        if (expireDays < 1 || expireDays >= 365 * 5) {
            throw new BusinessException(ErrorCode.POLICY_VIOLATION);
        }
    }

    private void validateOwnership(User requestUser, User owner) {
        if (!requestUser.getId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.POINT_NOT_FOUND);
        }
    }

    private String generatePointKey() {
        return UUID.randomUUID().toString();
    }
}