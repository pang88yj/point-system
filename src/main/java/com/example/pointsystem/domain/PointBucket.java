package com.example.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_bucket")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class PointBucket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String pointKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk", nullable = false)
    private User user;

    @Column(nullable = false)
    private int originalAmount;

    @Column(nullable = false)
    private int remainingAmount;

    @Column(nullable = false)
    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointBucketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_ledger_id", nullable = false)
    private PointLedger originLedger;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static PointBucket create(
            String pointKey,
            User user,
            int amount,
            LocalDateTime expireAt,
            PointSourceType sourceType,
            PointLedger originLedger
    ) {
        return PointBucket.builder()
                .pointKey(pointKey)
                .user(user)
                .originalAmount(amount)
                .remainingAmount(amount)
                .expireAt(expireAt)
                .sourceType(sourceType)
                .status(PointBucketStatus.ACTIVE)
                .originLedger(originLedger)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void deduct(int amount) {
        this.remainingAmount -= amount;
        if (this.remainingAmount < 0) {
            throw new IllegalStateException("remainingAmount cannot be negative");
        }
        if (this.remainingAmount == 0) {
            this.status = PointBucketStatus.DEPLETED;
        }
    }

    public void restore(int amount) {
        this.remainingAmount += amount;
        if (this.remainingAmount > 0 && this.status != PointBucketStatus.CANCELLED) {
            this.status = PointBucketStatus.ACTIVE;
        }
    }

    public void cancel() {
        this.remainingAmount = 0;
        this.status = PointBucketStatus.CANCELLED;
    }

    public boolean isExpired(LocalDateTime now) {
        return expireAt.isBefore(now);
    }

    public void expireIfNeeded(LocalDateTime now) {
        if (isExpired(now) && status != PointBucketStatus.CANCELLED) {
            status = PointBucketStatus.EXPIRED;
        }
    }

    public boolean isFullyUnused() {
        return originalAmount == remainingAmount;
    }
}
