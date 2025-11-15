package com.teamfiv5.fiv5.controller;

import com.teamfiv5.fiv5.dto.GuestbookDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.global.response.CustomResponse;
import com.teamfiv5.fiv5.global.security.AuthenticatedUser;
import com.teamfiv5.fiv5.service.GuestbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Guestbook (Timeline)", description = "방명록(타임라인) API")
@RestController
@RequestMapping("/api/v1/guestbook")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    @Operation(summary = "1. 방명록 작성",
            description = "다른 사용자(hostId)의 방명록에 글을 작성합니다. (블루투스 근접 확인은 클라이언트가 담당)")
    @PostMapping("/{hostId}")
    public ResponseEntity<CustomResponse<GuestbookDto.GuestbookResponse>> createGuestbookEntry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long hostId,
            @Valid @RequestBody GuestbookDto.GuestbookCreateRequest request
    ) {
        Long authorId = getUserId(user);
        GuestbookDto.GuestbookResponse response = guestbookService.createEntry(authorId, hostId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(response));
    }

    @Operation(summary = "2. 특정 유저의 방명록 목록 조회",
            description = "특정 사용자(hostId)의 방명록을 최신순으로 조회합니다.")
    @GetMapping("/{hostId}")
    public ResponseEntity<CustomResponse<List<GuestbookDto.GuestbookResponse>>> getGuestbookEntries(
            @PathVariable Long hostId
    ) {
        List<GuestbookDto.GuestbookResponse> response = guestbookService.getGuestbook(hostId);
        return ResponseEntity.ok(CustomResponse.ok(response));
    }

    @Operation(summary = "3. 방명록 삭제",
            description = "특정 방명록 항목(entryId)을 삭제합니다. (방명록 주인 또는 작성자 본인만 가능)")
    @DeleteMapping("/{entryId}")
    public ResponseEntity<CustomResponse<Void>> deleteGuestbookEntry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long entryId
    ) {
        Long currentUserId = getUserId(user);
        guestbookService.deleteEntry(currentUserId, entryId);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }
}