package com.teamloci.loci.dto;

import com.teamloci.loci.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    @Getter
    @NoArgsConstructor
    public static class ProfileUpdateRequest {

        @Schema(description = "고유 핸들 (ID)", example = "happy_quokka")
        @Pattern(regexp = "^[a-z0-9._]+$", message = "핸들은 영문 소문자, 숫자, 마침표(.), 밑줄(_)만 사용할 수 있습니다.")
        private String handle;

        @Schema(description = "표시 이름 (닉네임)", example = "행복한 쿼카")
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    public static class ProfileUrlUpdateRequest {
        private String profileUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class FcmTokenUpdateRequest {
        @NotBlank(message = "FCM 토큰이 필요합니다.")
        private String fcmToken;
    }

    @Getter
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String handle;
        private String nickname;
        private String profileUrl;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getHandle(),
                    user.getNickname(),
                    user.getProfileUrl(),
                    user.getCreatedAt()
            );
        }
    }
}