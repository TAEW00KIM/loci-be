package com.teamfiv5.fiv5.controller;

import com.teamfiv5.fiv5.dto.ChatDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.global.response.CustomResponse;
import com.teamfiv5.fiv5.global.security.AuthenticatedUser;
import com.teamfiv5.fiv5.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Tag(name = "Chat", description = "DM(채팅) API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    @Operation(summary = "[Chat] 1. (Write) 메시지 전송",
            description = "특정 상대방(receiverId)에게 메시지를 전송합니다. (Firestore '쓰기' 및 FCM 전송)")
    @PostMapping("/messages")
    public ResponseEntity<CustomResponse<Void>> sendMessage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ChatDto.SendMessageRequest request
    ) {
        Long myUserId = getUserId(user);
        chatService.sendMessage(
                myUserId,
                request.getReceiverId(),
                request.getMessageText()
        );
        return ResponseEntity.ok(CustomResponse.ok(null));
    }

    @Operation(summary = "[Chat] 2. (Read) 내 채팅방 목록 조회",
            description = "내가 참여하고 있는 채팅방 목록을 최신순으로 조회합니다. (Firestore '읽기')")
    @GetMapping("/rooms")
    public ResponseEntity<CustomResponse<List<ChatDto.ChatRoomResponse>>> getMyChatRooms(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long myUserId = getUserId(user);
        List<ChatDto.ChatRoomResponse> chatRooms = chatService.getChatRooms(myUserId);
        return ResponseEntity.ok(CustomResponse.ok(chatRooms));
    }
}