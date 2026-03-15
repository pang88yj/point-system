package com.example.pointsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UseAllocationDto {
    private String sourcePointKey;
    private int amount;
}