package com.teamfiv5.fiv5.controller;

import com.teamfiv5.fiv5.dto.AppleLoginRequest;
import com.teamfiv5.fiv5.dto.AuthResponse;
import com.teamfiv5.fiv5.global.response.CustomResponse;
import com.teamfiv5.fiv5.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/apple")
    public ResponseEntity<CustomResponse<AuthResponse>> loginWithApple(@RequestBody AppleLoginRequest request) { // ◀◀ 2. 반환 타입 AuthResponse
        AuthResponse authResponse = authService.loginWithApple(request);
        return ResponseEntity.ok(CustomResponse.ok(authResponse));
    }
}