package com.teamfiv5.fiv5.service;

import com.teamfiv5.fiv5.domain.Friendship;
import com.teamfiv5.fiv5.domain.FriendshipStatus;
import com.teamfiv5.fiv5.domain.User;
import com.teamfiv5.fiv5.dto.FriendDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.repository.FriendshipRepository;
import com.teamfiv5.fiv5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private static final SecureRandom random = new SecureRandom();

    // 공용: 사용자 ID로 User 엔티티 조회
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // --- 1. 블루투스 친구 찾기 ---

    /**
     * (API 1 - 시작) 내 블루투스 토큰 발급/갱신
     */
    @Transactional
    public FriendDto.DiscoveryTokenResponse refreshBluetoothToken(Long myUserId) {
        User user = findUserById(myUserId);

        byte[] tokenBytes = new byte[8];
        random.nextBytes(tokenBytes);
        String newToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        user.updateBluetoothToken(newToken);
        return new FriendDto.DiscoveryTokenResponse(newToken);
    }

    /**
     * (API 1 - 중지) 내 블루투스 토큰 만료
     */
    @Transactional
    public void stopBluetoothToken(Long myUserId) {
        User user = findUserById(myUserId);
        user.updateBluetoothToken(null);
    }

    /**
     * (API 2) 토큰 목록으로 사용자 목록 및 친구 상태 조회
     */
    @Transactional(readOnly = true)
    public List<FriendDto.DiscoveredUserResponse> findUsersByTokens(Long myUserId, List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        // 1. 토큰으로 사용자 조회
        List<User> users = userRepository.findByBluetoothTokenIn(tokens);

        // 2. 내 모든 친구 관계(요청 포함)를 미리 조회 (N+1 문제 방지)
        Map<Long, Friendship> friendshipMap = friendshipRepository.findAllFriendshipsByUserId(myUserId)
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getRequester().getId().equals(myUserId) ? f.getReceiver().getId() : f.getRequester().getId(),
                        Function.identity()
                ));

        // 3. (중요) 조회된 사용자와 내 친구 관계를 매핑
        return users.stream()
                .filter(user -> !user.getId().equals(myUserId))
                .map(user -> {
                    Friendship friendship = friendshipMap.get(user.getId());
                    FriendDto.FriendshipStatusInfo statusInfo = calculateFriendshipStatus(myUserId, friendship);
                    return FriendDto.DiscoveredUserResponse.of(user, statusInfo);
                })
                .collect(Collectors.toList());
    }

    // (로직) 친구 상태 계산
    private FriendDto.FriendshipStatusInfo calculateFriendshipStatus(Long myUserId, Friendship friendship) {
        if (friendship == null) {
            return FriendDto.FriendshipStatusInfo.NONE;
        }

        if (friendship.getStatus() == FriendshipStatus.FRIENDSHIP) {
            return FriendDto.FriendshipStatusInfo.FRIEND;
        }

        // PENDING 상태일 때, 내가 요청자인지(requester) 수신자인지(receiver) 확인
        if (friendship.getRequester().getId().equals(myUserId)) {
            return FriendDto.FriendshipStatusInfo.PENDING_ME_TO_THEM;
        } else {
            return FriendDto.FriendshipStatusInfo.PENDING_THEM_TO_ME;
        }
    }

    // --- 2. 친구 요청/수락 ---

    /**
     * (API 3) 친구 요청 (수정됨)
     */
    @Transactional
    public void requestFriend(Long myUserId, String targetToken) {
        // 1. 토큰으로 상대방 조회
        User target = userRepository.findByBluetoothToken(targetToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TARGET_TOKEN));

        // 2. 자신에게 요청하는지 확인 (ID 비교)
        if (myUserId.equals(target.getId())) {
            throw new CustomException(ErrorCode.SELF_FRIEND_REQUEST);
        }

        User me = findUserById(myUserId);

        // 3. (예외 처리) A->B, B->A 중복 요청 방지 (ID 비교)
        List<Friendship> existing = friendshipRepository.findAllFriendshipsByUserId(myUserId);
        boolean alreadyExists = existing.stream()
                .anyMatch(f -> f.getRequester().getId().equals(target.getId()) || f.getReceiver().getId().equals(target.getId()));

        if (alreadyExists) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
        }

        // 4. (보안) 요청에 사용된 토큰은 즉시 만료(null)시켜 재사용 방지
        target.updateBluetoothToken(null);

        // 5. 친구 요청 생성
        Friendship friendship = Friendship.builder()
                .requester(me)
                .receiver(target)
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);

        // TODO: 알림 생성 로직 추가
    }

    /**
     * (API 4) 친구 수락
     */
    @Transactional
    public void acceptFriend(Long myUserId, Long requesterId) {
        // (참고) 친구 수락 API는 이미 알림/요청 목록을 받은 상태이므로 id(PK)를 사용해도 보안상 안전합니다.
        Friendship friendship = friendshipRepository
                .findByRequesterIdAndReceiverIdAndStatus(requesterId, myUserId, FriendshipStatus.PENDING)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        friendship.accept();
    }
}