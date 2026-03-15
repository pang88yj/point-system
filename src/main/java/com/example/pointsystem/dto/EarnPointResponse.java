package com.example.pointsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EarnPointResponse {
    private String pointKey;
    private String userId;
    private int amount;
    private int remainingAmount;
    private LocalDateTime expireAt;
}