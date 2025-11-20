package com.teamloci.loci.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneLoginRequest {

    @Schema(description = "Firebase 클라이언트 SDK에서 받은 ID Token (JWT)", required = true)
    @NotBlank(message = "Firebase ID Token이 필요합니다.")
    private String idToken;
}