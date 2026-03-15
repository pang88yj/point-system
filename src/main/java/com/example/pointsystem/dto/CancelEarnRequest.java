package com.example.pointsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelEarnRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String pointKey;
}