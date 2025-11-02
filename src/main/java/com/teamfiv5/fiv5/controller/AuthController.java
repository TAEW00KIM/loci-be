package com.teamfiv5.fiv5.controller;

import com.teamfiv5.fiv5.dto.AppleLoginRequest;
import com.teamfiv5.fiv5.dto.AuthResponse;
import com.teamfiv5.fiv5.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth") // 인증 관련 API 경로
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/apple")
    public ResponseEntity<AuthResponse> loginWithApple(@RequestBody AppleLoginRequest request) {
        AuthResponse authResponse = authService.loginWithApple(request);
        return ResponseEntity.ok(authResponse);
    }
}