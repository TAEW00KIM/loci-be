package com.teamloci.loci.controller;

import com.teamloci.loci.dto.AuthResponse;
import com.teamloci.loci.dto.PhoneLoginRequest;
import com.teamloci.loci.global.response.CustomResponse;
import com.teamloci.loci.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 및 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "[Auth] 1. 전화번호 로그인 (Firebase)",
            description = "클라이언트가 Firebase 인증 후 받은 ID Token을 전송하면, 서버 검증 후 서비스 JWT를 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인/회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "COMMON200",
                                      "result": {
                                        "accessToken": "eyJh...[서비스 JWT]...",
                                        "isNewUser": true
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "(COMMON400) ID Token 누락", content = @Content),
            @ApiResponse(responseCode = "401", description = "(AUTH401) Firebase 토큰 검증 실패", content = @Content)
    })
    @PostMapping("/login/phone")
    public ResponseEntity<CustomResponse<AuthResponse>> loginWithPhone(
            @Valid @RequestBody PhoneLoginRequest request
    ) {
        AuthResponse authResponse = authService.loginWithPhone(request);
        return ResponseEntity.ok(CustomResponse.ok(authResponse));
    }
}