package com.example.pointsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelRestoreDto {
    private String targetPointKey;
    private int amount;
    private String restoreType;
}