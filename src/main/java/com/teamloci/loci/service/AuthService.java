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

    private static final SecureRandom random = new SecureRandom();
    private static final HexFormat hexFormat = HexFormat.of();

    @Transactional(readOnly = true)
    public AuthResponse loginWithPhone(PhoneLoginRequest request) {
        String phoneNumber = verifyFirebaseToken(request.getIdToken());
        String searchHash = aesUtil.hash(phoneNumber);

        Optional<User> userOptional = userRepository.findByPhoneSearchHash(searchHash);

        if (userOptional.isEmpty()) {
            return new AuthResponse(null, true);
        }

        User user = userOptional.get();
        String accessToken = jwtTokenProvider.createAccessToken(user);
        return new AuthResponse(accessToken, false);
    }

    @Transactional
    public void signUpWithPhone(PhoneLoginRequest request) {
        String idToken = request.getIdToken();
        String inputHandle = request.getHandle();
        String inputNickname = request.getNickname();
        String inputCountryCode = StringUtils.hasText(request.getCountryCode()) ? request.getCountryCode() : "KR";

        if (!StringUtils.hasText(inputHandle) || !StringUtils.hasText(inputNickname)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String phoneNumber = verifyFirebaseToken(idToken);

        log.info(">>> [회원가입] Firebase 추출 전화번호: {}", phoneNumber);

        String searchHash = aesUtil.hash(phoneNumber);

        String encryptedPhone = aesUtil.encrypt(phoneNumber);
        log.info(">>> [회원가입] DB 저장될 암호문: {}", encryptedPhone);

        if (userRepository.findByPhoneSearchHash(searchHash).isPresent()) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_USING);
        }

        if (userRepository.existsByHandle(inputHandle)) {
            throw new CustomException(ErrorCode.HANDLE_DUPLICATED);
        }

        User user = userRepository.save(
                User.builder()
                        .phoneEncrypted(encryptedPhone)
                        .phoneSearchHash(searchHash)
                        .handle(inputHandle)
                        .nickname(inputNickname)
                        .countryCode(inputCountryCode)
                        .build()
        );

        if (user.getBluetoothToken() == null) {
            user.updateBluetoothToken(generateUniqueBluetoothToken());
        }
    }


    private String verifyFirebaseToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String phoneNumber = (String) decodedToken.getClaims().get("phone_number");

            if (!StringUtils.hasText(phoneNumber)) {
                throw new CustomException(ErrorCode.NO_PHONE_NUMBER);
            }
            return phoneNumber;
        } catch (FirebaseAuthException e) {
            log.error("Firebase 토큰 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FIREBASE_AUTH_FAILED);
        }
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