package com.teamfiv5.fiv5.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleLoginRequest {
    private String identityToken;
    private String email;
    private String fullName;
}