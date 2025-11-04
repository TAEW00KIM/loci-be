package com.teamfiv5.fiv5.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자 추가
public class RefreshTokenRequest {
    private String refreshToken;
}