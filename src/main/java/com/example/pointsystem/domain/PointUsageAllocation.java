package com.example.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_usage_allocation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class PointUsageAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_ledger_id", nullable = false)
    private PointLedger useLedger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_bucket_id", nullable = false)
    private PointBucket sourceBucket;

    @Column(nullable = false)
    private int usedAmount;

    @Column(nullable = false)
    private int cancelledAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static PointUsageAllocation create(PointLedger useLedger, PointBucket sourceBucket, int usedAmount) {
        return PointUsageAllocation.builder()
                .useLedger(useLedger)
                .sourceBucket(sourceBucket)
                .usedAmount(usedAmount)
                .cancelledAmount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public int getCancelableAmount() {
        return usedAmount - cancelledAmount;
    }

    public void cancel(int amount) {
        this.cancelledAmount += amount;
        if (this.cancelledAmount > this.usedAmount) {
            throw new IllegalStateException("cancelledAmount cannot exceed usedAmount");
        }
    }
}
