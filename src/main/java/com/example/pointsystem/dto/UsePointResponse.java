package com.example.pointsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UsePointResponse {
    private String pointKey;
    private String orderNo;
    private int usedAmount;
    private List<UseAllocationDto> allocations;
}