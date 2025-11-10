package com.teamfiv5.fiv5.repository;

import com.teamfiv5.fiv5.domain.Friendship;
import com.teamfiv5.fiv5.domain.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 내가 요청했거나, 내가 받은 모든 친구 관계 조회
    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.requester.id = :userId OR f.receiver.id = :userId)")
    List<Friendship> findAllFriendshipsByUserId(@Param("userId") Long userId);

    // 두 사용자 간의 PENDING 상태인 요청 조회 (수락/거절 시 사용)
    Optional<Friendship> findByRequesterIdAndReceiverIdAndStatus(
            Long requesterId,
            Long receiverId,
            FriendshipStatus status
    );
}