package com.teamfiv5.fiv5.service;

import com.teamfiv5.fiv5.config.apple.AppleTokenVerifier;
import com.teamfiv5.fiv5.config.jwt.JwtTokenProvider;
import com.teamfiv5.fiv5.domain.User;
import com.teamfiv5.fiv5.dto.AppleLoginRequest;
import com.teamfiv5.fiv5.dto.AuthResponse;
import com.teamfiv5.fiv5.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AppleTokenVerifier appleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String APPLE_PROVIDER = "apple";

    @Transactional
    public AuthResponse loginWithApple(AppleLoginRequest request) {
        // 1. Apple ID 토큰 검증
        Claims claims = appleTokenVerifier.verify(request.getIdentityToken());
        String providerId = claims.getSubject(); // Apple의 고유 사용자 ID (sub)

        // 2. DB에서 사용자 조회
        boolean isNewUser = false;
        User user = userRepository.findByProviderIdAndProvider(providerId, APPLE_PROVIDER)
                .orElseGet(() -> {
                    // 3. 신규 사용자일 경우 가입
                    // 프론트에서 unique 처리를 한다고 하셨지만,
                    // 닉네임은 DB에서도 unique해야 하므로 임시 닉네임(예: apple_providerId)을 부여합니다.
                    // Apple은 이메일/이름을 최초 1회만 제공하므로, request에서 받아 사용합니다.
                    String email = request.getEmail();
                    String nickname = (request.getFullName() != null) ? request.getFullName() : "user_" + providerId;

                    // TODO: 닉네임 중복 시 예외 처리 또는 랜덤 닉네임 생성

                    return userRepository.save(
                            User.builder()
                                    .email(email)
                                    .nickname(nickname)
                                    .provider(APPLE_PROVIDER)
                                    .providerId(providerId)
                                    // profileUrl은 현재 Apple에서 제공하지 않음
                                    .build()
                    );
                });

        // 4. 우리 서비스의 JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user);

        return new AuthResponse(accessToken, isNewUser);
    }
}