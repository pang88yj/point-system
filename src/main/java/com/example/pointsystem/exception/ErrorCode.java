package com.example.pointsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    POLICY_NOT_FOUND("POLICY_NOT_FOUND", "포인트 정책이 존재하지 않습니다."),
    POINT_NOT_FOUND("POINT_NOT_FOUND", "포인트 내역을 찾을 수 없습니다."),
    INVALID_AMOUNT("INVALID_AMOUNT", "유효하지 않은 포인트 금액입니다."),
    POLICY_VIOLATION("POLICY_VIOLATION", "포인트 정책을 위반했습니다."),
    INSUFFICIENT_POINT("INSUFFICIENT_POINT", "사용 가능한 포인트가 부족합니다."),
    ALREADY_USED_POINT("ALREADY_USED_POINT", "이미 사용된 적립건은 취소할 수 없습니다."),
    INVALID_CANCEL_AMOUNT("INVALID_CANCEL_AMOUNT", "취소 가능한 금액을 초과했습니다."),
    INVALID_POINT_TYPE("INVALID_POINT_TYPE", "잘못된 포인트 타입입니다.");

    private final String code;
    private final String message;
}