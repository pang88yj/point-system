package com.example.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class PointPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int maxEarnPerTxn;

    @Column(nullable = false)
    private int maxBalancePerUser;

    @Column(nullable = false)
    private int defaultExpireDays;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static PointPolicy createDefault(int maxEarnPerTxn, int maxBalancePerUser, int defaultExpireDays) {
        LocalDateTime now = LocalDateTime.now();
        return PointPolicy.builder()
                .maxEarnPerTxn(maxEarnPerTxn)
                .maxBalancePerUser(maxBalancePerUser)
                .defaultExpireDays(defaultExpireDays)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
