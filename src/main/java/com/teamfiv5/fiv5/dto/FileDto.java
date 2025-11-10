package com.teamfiv5.fiv5.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FileDto {

    /**
     * S3 업로드 완료 후 URL 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class FileUploadResponse {
        private String fileUrl; // S3에 업로드된 최종 URL
    }
}