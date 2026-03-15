package com.example.pointsystem.controller;

import com.example.pointsystem.dto.*;
import com.example.pointsystem.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn")
    public ApiResponse<EarnPointResponse> earn(@RequestBody @Valid EarnPointRequest request) {
        return new ApiResponse<>(true, pointService.earn(request));
    }

    @PostMapping("/earn-cancel")
    public ApiResponse<String> cancelEarn(@RequestBody @Valid CancelEarnRequest request) {
        pointService.cancelEarn(request);
        return new ApiResponse<>(true, "적립 취소가 완료되었습니다.");
    }

    @PostMapping("/use")
    public ApiResponse<UsePointResponse> use(@RequestBody @Valid UsePointRequest request) {
        return new ApiResponse<>(true, pointService.use(request));
    }

    @PostMapping("/use-cancel")
    public ApiResponse<CancelUseResponse> cancelUse(@RequestBody @Valid CancelUseRequest request) {
        return new ApiResponse<>(true, pointService.cancelUse(request));
    }

    @GetMapping("/balance/{userId}")
    public ApiResponse<BalanceResponse> getBalance(@PathVariable String userId) {
        return new ApiResponse<>(true, pointService.getBalance(userId));
    }
}