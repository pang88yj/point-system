package com.example.pointsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsePointRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String orderNo;

    @Min(1)
    private int amount;
}