// 경로: src/main/java/com/teamfiv5/fiv5/global/exception/code/ErrorCode.java
package com.teamfiv5.fiv5.global.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode {

    // == 기본 에러 ==
    INVALID_REQUEST("COMMON400", "요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON403", "접근이 금지되었습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("COMMON404", "요청한 자원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("COMMON500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // == Auth 관련 에러 ==
    APPLE_TOKEN_VERIFY_FAILED("AUTH401_1", "Apple ID 토큰 검증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    NICKNAME_DUPLICATED("AUTH409_1", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),

    // == User 관련 에러 ==
    USER_NOT_FOUND("USER404_1", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // == S3 관련 에러 ==
    FILE_IS_EMPTY("FILE400_1", "업로드할 파일이 비어있습니다.", HttpStatus.BAD_REQUEST),
    S3_UPLOAD_FAILED("S3500_1", "S3 파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}