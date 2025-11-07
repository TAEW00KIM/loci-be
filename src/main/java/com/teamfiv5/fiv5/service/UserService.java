// 경로: src/main/java/com/teamfiv5/fiv5/service/UserService.java
package com.teamfiv5.fiv5.service;

import com.teamfiv5.fiv5.domain.User;
import com.teamfiv5.fiv5.dto.UserDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;

    // 공용: 사용자 ID로 User 엔티티 조회 (없으면 예외)
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 내 정보 조회
    public UserDto.UserResponse getMyInfo(Long userId) {
        User user = findUserById(userId);
        return UserDto.UserResponse.from(user);
    }

    // 기능 1: 닉네임 + bio 변경 (fetchUser)
    @Transactional
    public UserDto.UserResponse updateProfile(Long userId, UserDto.ProfileUpdateRequest request) {
        User user = findUserById(userId);
        String newNickname = request.getNickname();
        String newBio = request.getBio();

        // 닉네임이 현재 닉네임과 다를 때만 중복 검사
        if (!user.getNickname().equals(newNickname)) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
            }
        }

        user.updateProfile(newNickname, newBio); // 닉네임과 bio 동시 업데이트
        return UserDto.UserResponse.from(user); // 수정된 유저 정보 반환
    }

    // 기능 2: 프로필 사진 변경/삭제 (fetchProfile)
    @Transactional
    public UserDto.UserResponse updateProfileUrl(Long userId, MultipartFile profileImage) {
        User user = findUserById(userId);
        String oldFileUrl = user.getProfileUrl();
        String newFileUrl = null;

        // 1. (변경) 새 파일이 (profileImage) 들어온 경우
        if (profileImage != null && !profileImage.isEmpty()) {
            // S3에 업로드하고 새 URL을 받음
            newFileUrl = s3UploadService.upload(profileImage, "profiles");
        }
        // 2. (삭제) 파일이 안 들어온 경우 (요청에 파일이 없음) -> null로 설정
        else {
            newFileUrl = null;
        }

        // 3. DB에 새 URL 저장 (null일 수도 있음)
        user.updateProfileUrl(newFileUrl);

        // 4. (중요) 기존 파일이 있었고, 새 파일로 변경되거나 삭제되는 경우
        if (oldFileUrl != null && (newFileUrl == null || !oldFileUrl.equals(newFileUrl))) {
            s3UploadService.delete(oldFileUrl);
        }

        return UserDto.UserResponse.from(user); // 수정된 유저 정보 반환
    }

    // 기능 3: 회원 탈퇴 (Soft Delete)
    @Transactional
    public void withdrawUser(Long userId) {
        User user = findUserById(userId);
        user.withdraw();
    }
}