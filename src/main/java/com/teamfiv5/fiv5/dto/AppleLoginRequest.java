package com.teamfiv5.fiv5.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleLoginRequest {
    private String identityToken; // 클라이언트(iOS)에서 받은 Apple ID 토큰
    private String email; // (선택) 최초 로그인 시 이메일
    private String fullName; // (선택) 최초 로그인 시 이름 (닉네임으로 활용)
}