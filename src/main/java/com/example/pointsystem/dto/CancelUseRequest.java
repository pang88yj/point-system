package com.example.pointsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelUseRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String pointKey;

    @Min(1)
    private int cancelAmount;
}