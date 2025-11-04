package com.teamfiv5.fiv5.service;

import com.teamfiv5.fiv5.config.apple.AppleTokenVerifier;
import com.teamfiv5.fiv5.config.jwt.JwtTokenProvider;
import com.teamfiv5.fiv5.domain.User;
import com.teamfiv5.fiv5.dto.AppleLoginRequest;
import com.teamfiv5.fiv5.dto.AuthResponse;
import com.teamfiv5.fiv5.dto.TokenResponse;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String APPLE_PROVIDER = "apple";

    @Transactional
    public AuthResponse loginWithApple(AppleLoginRequest request) {

        // 2. DB에서 사용자 조회
        String providerId = request.getIdentityToken();

        if (!StringUtils.hasText(providerId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        boolean isNewUser = false;
        User user = userRepository.findByProviderIdAndProvider(providerId, APPLE_PROVIDER)
                .orElseGet(() -> {
                    // 8. 신규 사용자일 경우 (orElseGet 실행됨)
                    String email = request.getEmail(); // (nullable)
                    String nickname = (request.getFullName() != null) ? request.getFullName() : "user_" + providerId;

                    // (닉네임 중복 체크 로직 필요)

                    return userRepository.save(
                            User.builder()
                                    .email(email) // ◀◀ email 저장
                                    .nickname(nickname) // ◀◀ fullName을 nickname으로 저장
                                    .provider(APPLE_PROVIDER)
                                    .providerId(providerId)
                                    .build()
                    );
                });

        // 9. DB에서 찾았는데 닉네임이 임시값이면 신규 유저로 판단
        // (Apple은 fullName을 안 줄 수도 있으므로 임시 닉네임 기준)
        if (("user_" + providerId).equals(user.getNickname())) {
            isNewUser = true;
        }

        // 10. 우리 서비스의 JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user);
        // (AuthResponse는 RefreshToken이 없으므로 생성 안 함)

        return new AuthResponse(accessToken, isNewUser); // ◀◀ 11. AuthResponse 반환
    }

//    @Transactional(readOnly = true)
//    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
//        String refreshToken = request.getRefreshToken();
//
//        if (!jwtTokenProvider.validateToken(refreshToken)) {
//            throw new CustomException(ErrorCode.UNAUTHORIZED);
//        }
//
//        String userIdStr = jwtTokenProvider.getUserIdFromToken(refreshToken);
//        Long userId = Long.parseLong(userIdStr);
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//        String newAccessToken = jwtTokenProvider.createAccessToken(user);
//
//        return new TokenResponse(newAccessToken, refreshToken);
//    }
}