package com.example.pointsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EarnPointRequest {

    @NotBlank
    private String userId;

    @Min(1)
    @Max(100000)
    private int amount;

    private Integer expireDays;

    private boolean manual;

    private String description;
}