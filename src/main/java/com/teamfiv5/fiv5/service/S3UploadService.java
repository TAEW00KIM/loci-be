// 경로: src/main/java/com/teamfiv5/fiv5/service/S3UploadService.java
package com.teamfiv5.fiv5.service;

import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    // (자동 주입) 'spring-cloud-aws-starter-s3'가 S3Client Bean을 자동으로 생성해 줍니다.
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket:fiv5-assets}")
    private String bucket;
    /**
     * 파일을 S3에 업로드하고, 생성된 URL을 반환합니다.
     * @param file 업로드할 파일
     * @param dirName 버킷 내의 디렉토리 이름 (예: "profiles")
     * @return S3에 업로드된 파일의 전체 URL
     */
    public String upload(MultipartFile file, String dirName) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_IS_EMPTY);
        }

        String original = Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> !name.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("파일 이름이 없습니다."));

        // 파일 이름 중복 방지를 위해 UUID 사용
        String uniqueName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        String key = dirName + "/" + uniqueName; // S3 내 최종 경로 (예: profiles/uuid_image.jpg)

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .cacheControl("public, max-age=31536000") // 1년간 캐시
                .build();

        try {
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (SdkException | IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }

        // 업로드된 파일의 URL 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toString();
    }

    /**
     * S3에서 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // URL에서 S3 'key' (파일 경로) 추출
            String key = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            // 삭제 실패 시 로그만 남김 (이미 삭제되었거나, URL이 잘못되었을 수 있음)
            System.err.println("S3 파일 삭제 실패: " + fileUrl + " (" + e.getMessage() + ")");
        }
    }
}