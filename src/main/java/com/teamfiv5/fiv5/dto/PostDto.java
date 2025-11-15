package com.teamfiv5.fiv5.dto;

import com.teamfiv5.fiv5.domain.MediaType;
import com.teamfiv5.fiv5.domain.Post;
import com.teamfiv5.fiv5.domain.PostCollaborator;
import com.teamfiv5.fiv5.domain.PostMedia;
import com.teamfiv5.fiv5.domain.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSimpleResponse {
        private Long id;
        private String nickname;
        private String profileUrl;

        public static UserSimpleResponse from(User user) {
            return new UserSimpleResponse(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileUrl()
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MediaResponse {
        private Long id;
        private String mediaUrl;
        private MediaType mediaType;
        private int sortOrder;

        public static MediaResponse from(PostMedia media) {
            return new MediaResponse(
                    media.getId(),
                    media.getMediaUrl(),
                    media.getMediaType(),
                    media.getSortOrder()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    public static class MediaRequest {
        @NotEmpty
        private String mediaUrl;
        @NotNull
        private MediaType mediaType;
        @NotNull
        private Integer sortOrder;
    }

    @Getter
    @NoArgsConstructor
    public static class PostCreateRequest {
        private String contents; // 내용은 비어있을 수 있음
        private List<MediaRequest> mediaList; // 미디어 목록
        private List<Long> collaboratorIds; // 공동 작업자 User ID 목록
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor // (상세 조회 응답)
    public static class PostDetailResponse {
        private Long id;
        private String contents;
        private UserSimpleResponse author; // 작성자
        private List<MediaResponse> mediaList;
        private List<UserSimpleResponse> collaborators;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static PostDetailResponse from(Post post) {
            return PostDetailResponse.builder()
                    .id(post.getId())
                    .contents(post.getContents())
                    .author(UserSimpleResponse.from(post.getUser()))
                    .mediaList(post.getMediaList().stream()
                            .map(MediaResponse::from)
                            .collect(Collectors.toList()))
                    .collaborators(post.getCollaborators().stream()
                            .map(PostCollaborator::getUser)
                            .map(UserSimpleResponse::from)
                            .collect(Collectors.toList()))
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }
    }
}