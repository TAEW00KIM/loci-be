package com.teamfiv5.fiv5.controller;

import com.teamfiv5.fiv5.dto.PostDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.global.response.CustomResponse;
import com.teamfiv5.fiv5.global.security.AuthenticatedUser;
import com.teamfiv5.fiv5.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post", description = "포스트(게시물) API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    @Operation(summary = "1. 포스트 생성",
            description = "새로운 포스트를 생성합니다. (미디어, 공동 작업자 포함)")
    @PostMapping
    public ResponseEntity<CustomResponse<PostDto.PostDetailResponse>> createPost(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody PostDto.PostCreateRequest request
    ) {
        Long authorId = getUserId(user);
        PostDto.PostDetailResponse response = postService.createPost(authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(response));
    }

    @Operation(summary = "2. 포스트 상세 조회",
            description = "특정 포스트의 상세 정보를 조회합니다. (미디어, 공동 작업자 포함)")
    @GetMapping("/{postId}")
    public ResponseEntity<CustomResponse<PostDto.PostDetailResponse>> getPost(
            @PathVariable Long postId
    ) {
        PostDto.PostDetailResponse response = postService.getPost(postId);
        return ResponseEntity.ok(CustomResponse.ok(response));
    }

    @Operation(summary = "3. 특정 유저의 포스트 목록 조회",
            description = "특정 사용자 ID(userId)가 작성한 포스트 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomResponse<List<PostDto.PostDetailResponse>>> getPostsByUser(
            @PathVariable Long userId
    ) {
        List<PostDto.PostDetailResponse> response = postService.getPostsByUser(userId);
        return ResponseEntity.ok(CustomResponse.ok(response));
    }

    @Operation(summary = "4. 포스트 삭제",
            description = "특정 포스트를 삭제합니다. (작성자 본인만 가능)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<CustomResponse<Void>> deletePost(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long postId
    ) {
        Long currentUserId = getUserId(user);
        postService.deletePost(currentUserId, postId);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }
}