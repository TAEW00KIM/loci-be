package com.teamfiv5.fiv5.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken; // 우리 서비스의 JWT
    private Boolean isNewUser; // 신규 가입 여부 (프론트 참고용)
}