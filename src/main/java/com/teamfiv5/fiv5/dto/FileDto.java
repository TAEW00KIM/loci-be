package com.teamfiv5.fiv5.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FileDto {

    @Getter
    @AllArgsConstructor
    public static class FileUploadResponse {
        private String fileUrl;
    }
}