package com.teamloci.loci.controller;

import com.teamloci.loci.dto.UserDto;
import com.teamloci.loci.global.exception.CustomException;
import com.teamloci.loci.global.exception.code.ErrorCode;
import com.teamloci.loci.global.response.CustomResponse;
import com.teamloci.loci.global.security.AuthenticatedUser;
import com.teamloci.loci.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 프로필 정보 및 설정 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    @Operation(summary = "[User] 0. 핸들(ID) 중복 검사 (비로그인 가능)",
            description = "입력한 핸들(@ID)이 사용 가능한지 확인합니다. (중복이면 `isValidHandle: false`, 사용 가능하면 `true`)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "COMMON200",
                                      "result": {
                                        "isValidHandle": true
                                      }
                                    }
                                    """)))
    })
    @GetMapping("/check-handle")
    public ResponseEntity<CustomResponse<UserDto.HandleCheckResponse>> checkHandle(
            @Parameter(description = "검사할 핸들 (영문 소문자, 숫자, _, .)", required = true, example = "happy_quokka")
            @RequestParam String handle
    ) {
        boolean isAvailable = userService.checkHandleAvailability(handle);
        return ResponseEntity.ok(CustomResponse.ok(new UserDto.HandleCheckResponse(isAvailable)));
    }

    @Operation(summary = "[User] 1. 내 정보 조회",
            description = "현재 로그인한 사용자의 프로필 정보(핸들, 닉네임, 프사 등)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(value = """
                             {
                               "code": "COMMON200",
                               "result": {
                                 "id": 1,
                                 "handle": "happy_quokka",
                                 "nickname": "행복한 쿼카",
                                 "profileUrl": "https://fiv5-assets.s3.../profile.png",
                                 "createdAt": "2025-11-01T12:00:00"
                               }
                             }
                             """))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> getMyInfo(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = getUserId(user);
        UserDto.UserResponse myInfo = userService.getMyInfo(userId);
        return ResponseEntity.ok(CustomResponse.ok(myInfo));
    }

    @Operation(summary = "[User] 2. 프로필(핸들, 닉네임) 수정",
            description = "현재 사용자의 핸들(@ID) 또는 닉네임을 수정합니다. 변경하지 않을 값은 보내지 않아도 됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(value = """
                             {
                               "code": "COMMON200",
                               "result": {
                                 "id": 1,
                                 "handle": "new_handle",
                                 "nickname": "새로운 닉네임",
                                 "profileUrl": "https://fiv5-assets.s3.../profile.png",
                                 "createdAt": "2025-11-01T12:00:00"
                               }
                             }
                             """))),
            @ApiResponse(responseCode = "400", description = "(AUTH409_1) 이미 존재하는 핸들입니다.", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PatchMapping("/me/profile")
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 핸들 또는 닉네임 (둘 중 하나만 보내도 됨)",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "handle": "new_handle",
                              "nickname": "새로운 닉네임"
                            }
                            """))
            )
            @Valid @RequestBody UserDto.ProfileUpdateRequest request
    ) {
        Long userId = getUserId(user);
        UserDto.UserResponse updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(CustomResponse.ok(updatedUser));
    }

    @Operation(summary = "[User] 3. 프로필 사진 파일 업로드 및 변경 (Multipart)",
            description = "이미지 파일을 직접 전송하여 프로필 사진을 변경합니다. (파일 미전송 시 프로필 사진 삭제)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    @PatchMapping(value = "/me/profileUrl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> updateProfileUrl(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "업로드할 이미지 파일 (선택). 보내지 않으면 기존 사진이 삭제됨.")
            @RequestPart(value = "file", required = false) MultipartFile profileImage
    ) {
        Long userId = getUserId(user);
        UserDto.UserResponse updatedUser = userService.updateProfileUrl(userId, profileImage);
        return ResponseEntity.ok(CustomResponse.ok(updatedUser));
    }

    @Operation(summary = "[User] 4. 프로필 사진 URL 변경 (JSON)",
            description = "이미 S3에 업로드된 URL을 사용하여 프로필 사진을 변경합니다. (빈 문자열 전송 시 삭제)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = "{\"profileUrl\": \"https://fiv5-assets.s3.../new.jpg\"}"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    @PatchMapping(value = "/me/profileUrl", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> updateProfileUrlString(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UserDto.ProfileUrlUpdateRequest request
    ) {
        Long userId = getUserId(user);
        UserDto.UserResponse updatedUser = userService.updateProfileUrl(userId, request);
        return ResponseEntity.ok(CustomResponse.ok(updatedUser));
    }

    @Operation(summary = "[User] 5. 회원 탈퇴",
            description = "계정을 탈퇴 처리(Soft Delete)합니다. 핸들은 'deleted_{id}'로 변경되어 재사용 가능해집니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "탈퇴 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(value = "{ \"code\": \"COMMON200\", \"result\": null }")))
    })
    @DeleteMapping("/me")
    public ResponseEntity<CustomResponse<Void>> withdrawUser(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = getUserId(user);
        userService.withdrawUser(userId);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }

    @Operation(summary = "[User] 6. FCM 기기 토큰 갱신",
            description = "앱 실행/로그인 시 발급받은 FCM 토큰을 서버에 등록합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = "{\"fcmToken\": \"c_abc123...\"}"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "갱신 성공", content = @Content)
    })
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<CustomResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UserDto.FcmTokenUpdateRequest request
    ) {
        Long userId = getUserId(user);
        userService.updateFcmToken(userId, request);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }
}