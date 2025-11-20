package com.teamloci.loci.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.teamloci.loci.config.jwt.JwtTokenProvider;
import com.teamloci.loci.domain.User;
import com.teamloci.loci.dto.AuthResponse;
import com.teamloci.loci.dto.PhoneLoginRequest;
import com.teamloci.loci.global.exception.CustomException;
import com.teamloci.loci.global.exception.code.ErrorCode;
import com.teamloci.loci.global.util.AesUtil;
import com.teamloci.loci.global.util.RandomNicknameGenerator;
import com.teamloci.loci.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AesUtil aesUtil;

    private static final String PHONE_PROVIDER = "phone";
    private static final SecureRandom random = new SecureRandom();
    private static final HexFormat hexFormat = HexFormat.of();

    @Transactional
    public AuthResponse loginWithPhone(PhoneLoginRequest request) {
        String idToken = request.getIdToken();

        String phoneNumber;
        String uid;

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            uid = decodedToken.getUid();
            phoneNumber = (String) decodedToken.getClaims().get("phone_number");

            if (!StringUtils.hasText(phoneNumber)) {
                throw new CustomException(ErrorCode.NO_PHONE_NUMBER);
            }

        } catch (FirebaseAuthException e) {
            log.error("Firebase 토큰 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FIREBASE_AUTH_FAILED);
        }

        String searchHash = aesUtil.hash(phoneNumber);
        Optional<User> userOptional = userRepository.findByPhoneSearchHash(searchHash);

        final boolean isNewUser = userOptional.isEmpty();
        User user;
        if (isNewUser) {
            String nickname = RandomNicknameGenerator.generate();

            if (userRepository.existsByNickname(nickname)) {
                nickname = nickname + "_" + uid.substring(0, 4);
            }

            user = userRepository.save(
                    User.builder()
                            .phoneEncrypted(aesUtil.encrypt(phoneNumber))
                            .phoneSearchHash(searchHash)
                            .nickname(nickname)
                            .provider(PHONE_PROVIDER)
                            .providerId(uid)
                            .email(null)
                            .build()
            );
        } else {
            user = userOptional.get();
        }

        if (user.getBluetoothToken() == null) {
            user.updateBluetoothToken(generateUniqueBluetoothToken());
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);

        return new AuthResponse(accessToken, isNewUser);
    }

    private String generateUniqueBluetoothToken() {
        byte[] tokenBytes = new byte[4];
        String newToken;
        do {
            random.nextBytes(tokenBytes);
            newToken = hexFormat.formatHex(tokenBytes);
        } while (userRepository.existsByBluetoothToken(newToken));

        return newToken;
    }
}