package com.teamloci.loci.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.teamloci.loci.domain.Friendship;
import com.teamloci.loci.domain.FriendshipStatus;
import com.teamloci.loci.domain.User;
import com.teamloci.loci.domain.UserStatus;
import com.teamloci.loci.dto.UserDto;
import com.teamloci.loci.global.exception.CustomException;
import com.teamloci.loci.global.exception.code.ErrorCode;
import com.teamloci.loci.global.util.AesUtil;
import com.teamloci.loci.repository.FriendshipRepository;
import com.teamloci.loci.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HexFormat;
import java.util.List;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class FriendServiceIntegrationTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private AesUtil aesUtil;

    @MockBean private Firestore firestore;
    @MockBean private FirebaseAuth firebaseAuth;
    @MockBean private FirebaseMessaging firebaseMessaging;
    @MockBean private NotificationService notificationService;
    @MockBean private S3Client s3Client;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        String phoneA = "+821011111111";
        String phoneB = "+821022222222";

        userA = User.builder()
                .handle("user_a")
                .nickname("UserA")
                .phoneSearchHash(aesUtil.hash(phoneA))
                .phoneEncrypted(aesUtil.encrypt(phoneA))
                .countryCode("KR")
                .build();

        userB = User.builder()
                .handle("user_b")
                .nickname("UserB")
                .phoneSearchHash(aesUtil.hash(phoneB))
                .phoneEncrypted(aesUtil.encrypt(phoneB))
                .countryCode("KR")
                .build();

        userA.updateBluetoothToken(generateTestToken());
        userB.updateBluetoothToken(generateTestToken());

        userRepository.saveAll(List.of(userA, userB));
    }

    private String generateTestToken() {
        SecureRandom random = new SecureRandom();
        HexFormat hexFormat = HexFormat.of();
        byte[] tokenBytes = new byte[4];
        String newToken;
        do {
            random.nextBytes(tokenBytes);
            newToken = hexFormat.formatHex(tokenBytes);
        } while (userRepository.existsByBluetoothToken(newToken));
        return newToken;
    }

    @Test
    @DisplayName("1. 연락처 매칭: 주소록 번호로 가입된 친구를 찾는다")
    void matchFriends_Success() {
        String rawPhoneNumber = "010-1234-5678";
        String e164PhoneNumber = "+821012345678";
        String searchHash = aesUtil.hash(e164PhoneNumber);

        User userC = User.builder()
                .handle("phone_friend")
                .nickname("PhoneFriend")
                .phoneSearchHash(searchHash)
                .phoneEncrypted(aesUtil.encrypt(e164PhoneNumber))
                .countryCode("KR")
                .build();
        userRepository.save(userC);

        List<String> myContacts = List.of(rawPhoneNumber, "010-9999-9999");

        List<UserDto.UserResponse> matched = friendService.matchFriends(userA.getId(), myContacts);

        assertThat(matched).hasSize(1);
        assertThat(matched.get(0).getNickname()).isEqualTo("PhoneFriend");
        assertThat(matched.get(0).getId()).isEqualTo(userC.getId());
    }

    @Test
    @DisplayName("2. 친구 추가: 요청 없이 즉시 친구 관계(FRIENDSHIP)가 된다")
    void addFriend_Success() {
        friendService.addFriend(userA.getId(), userB.getId());

        List<Friendship> friendships = friendshipRepository.findAll();
        assertThat(friendships).hasSize(1);

        Friendship friendship = friendships.get(0);
        assertThat(friendship.getRequester().getId()).isEqualTo(userA.getId());
        assertThat(friendship.getReceiver().getId()).isEqualTo(userB.getId());
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.FRIENDSHIP);
    }

    @Test
    @DisplayName("3. 친구 추가 실패: 이미 친구인 경우")
    void addFriend_Fail_AlreadyExists() {
        friendshipRepository.save(Friendship.builder()
                .requester(userA)
                .receiver(userB)
                .status(FriendshipStatus.FRIENDSHIP)
                .build());

        assertThatThrownBy(() -> friendService.addFriend(userA.getId(), userB.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("4. 친구 삭제: 관계가 DB에서 제거된다")
    void deleteFriend_Success() {
        friendshipRepository.save(Friendship.builder()
                .requester(userA)
                .receiver(userB)
                .status(FriendshipStatus.FRIENDSHIP)
                .build());

        friendService.deleteFriend(userA.getId(), userB.getId());

        assertThat(friendshipRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("5. 친구 목록 조회: 내가 맺은 친구들이 조회된다")
    void getMyFriends_Success() {
        friendshipRepository.save(Friendship.builder()
                .requester(userA)
                .receiver(userB)
                .status(FriendshipStatus.FRIENDSHIP)
                .build());

        List<UserDto.UserResponse> friends = friendService.getMyFriends(userA.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getNickname()).isEqualTo("UserB");
    }
}