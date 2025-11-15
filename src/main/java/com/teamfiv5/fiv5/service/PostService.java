package com.teamfiv5.fiv5.service;

import com.teamfiv5.fiv5.domain.*;
import com.teamfiv5.fiv5.dto.PostDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.repository.PostRepository;
import com.teamfiv5.fiv5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    @Transactional
    public PostDto.PostDetailResponse createPost(Long authorId, PostDto.PostCreateRequest request) {
        User author = findUserById(authorId);

        Post post = Post.builder()
                .user(author)
                .contents(request.getContents())
                .build();

        if (request.getMediaList() != null) {
            request.getMediaList().forEach(mediaReq -> {
                post.addMedia(PostMedia.builder()
                        .mediaUrl(mediaReq.getMediaUrl())
                        .mediaType(mediaReq.getMediaType())
                        .sortOrder(mediaReq.getSortOrder())
                        .build());
            });
        }

        if (request.getCollaboratorIds() != null) {
            request.getCollaboratorIds().forEach(collaboratorId -> {
                User collaboratorUser = findUserById(collaboratorId);
                post.addCollaborator(PostCollaborator.builder()
                        .user(collaboratorUser)
                        .build());
            });
        }

        Post savedPost = postRepository.save(post);

        return PostDto.PostDetailResponse.from(findPostById(savedPost.getId()));
    }

    public PostDto.PostDetailResponse getPost(Long postId) {
        Post post = findPostById(postId);
        return PostDto.PostDetailResponse.from(post);
    }

    public List<PostDto.PostDetailResponse> getPostsByUser(Long userId) {
        List<Post> posts = postRepository.findByUserIdWithUser(userId);

        return posts.stream()
                .map(post -> getPost(post.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(Long currentUserId, Long postId) {
        Post post = findPostById(postId);

        if (!post.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.NOT_POST_AUTHOR);
        }

        postRepository.delete(post);
    }

    // TODO: 포스트 수정(Update) 로직 추가
}