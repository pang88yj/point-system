package com.example.pointsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CancelUseResponse {
    private String pointKey;
    private int cancelAmount;
    private List<CancelRestoreDto> restored;
}