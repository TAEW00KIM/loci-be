package com.teamloci.loci.dto;

import com.teamloci.loci.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FriendDto {

    @Getter
    @NoArgsConstructor
    @Schema(description = "연락처 기반 친구 매칭 요청 Body")
    public static class ContactListRequest {
        @Schema(
                description = "주소록에 있는 전화번호 리스트. (형식 무관: '010-1234-5678', '+82 10 1234 5678' 등 모두 가능)",
                example = "[\"010-1234-5678\", \"+82 10-9999-8888\", \"01055556666\"]",
                required = true
        )
        @NotNull(message = "전화번호 리스트는 필수입니다.")
        private List<String> phoneNumbers;
    }

    @Getter
    @NoArgsConstructor
    public static class FindFriendsByTokensRequest {
        @NotNull
        private List<String> tokens;
    }

    @Getter
    @NoArgsConstructor
    public static class FriendManageByIdRequest { // (DTO 이름 수정)
        @NotNull(message = "상대방의 ID가 필요합니다.")
        private Long targetUserId; // 요청을 보낸 사람(requester) 또는 수락할 사람의 ID
    }

    @Getter
    @AllArgsConstructor
    public static class DiscoveryTokenResponse {
        private String bluetoothToken;
    }

    @Getter
    @AllArgsConstructor
    public static class DiscoveredUserResponse {
        private Long id;
        private String nickname;
        private String bio;
        private String profileUrl;

        private String bluetoothToken;

        private FriendshipStatusInfo friendshipStatus;

        public static DiscoveredUserResponse of(User user, FriendshipStatusInfo status) {
            return new DiscoveredUserResponse(
                    user.getId(),
                    user.getNickname(),
                    user.getBio(),
                    user.getProfileUrl(),
                    user.getBluetoothToken(),
                    status
            );
        }
    }

    public enum FriendshipStatusInfo {
        NONE, FRIEND, PENDING_ME_TO_THEM, PENDING_THEM_TO_ME
    }
}