package com.teamloci.loci.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneLoginRequest {

    @Schema(description = "Firebase 클라이언트 SDK에서 받은 ID Token (JWT)", required = true)
    @NotBlank(message = "Firebase ID Token이 필요합니다.")
    private String idToken;

    @Schema(description = "회원가입 시 사용할 핸들(ID). (로그인 시 생략 가능)", example = "happy_quokka")
    @Pattern(regexp = "^[a-z0-9._]*$", message = "핸들은 영문 소문자, 숫자, 마침표(.), 밑줄(_)만 사용할 수 있습니다.")
    private String handle;

    @Schema(description = "회원가입 시 사용할 닉네임. (로그인 시 생략 가능)", example = "행복한 쿼카")
    private String nickname;
}