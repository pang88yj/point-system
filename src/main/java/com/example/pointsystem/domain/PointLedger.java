package com.example.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class PointLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String pointKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointLedgerType type;

    @Column(nullable = false)
    private int amount;

    @Column(length = 100)
    private String orderNo;

    @Column(length = 100)
    private String relatedPointKey;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static PointLedger create(
            String pointKey,
            User user,
            PointLedgerType type,
            int amount,
            String orderNo,
            String relatedPointKey,
            String description
    ) {
        return PointLedger.builder()
                .pointKey(pointKey)
                .user(user)
                .type(type)
                .amount(amount)
                .orderNo(orderNo)
                .relatedPointKey(relatedPointKey)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
