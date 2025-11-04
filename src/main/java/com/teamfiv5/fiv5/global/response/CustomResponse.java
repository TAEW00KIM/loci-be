package com.teamfiv5.fiv5.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.teamfiv5.fiv5.global.exception.code.BaseErrorCode;
import com.teamfiv5.fiv5.global.exception.code.GeneralSuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"timestamp", "isSuccess", "code", "message", "result"})
public class CustomResponse<T> {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final Boolean isSuccess;
    private final String code;
    private final String message;
    private final T result;

    // 200 OK
    public static <T> CustomResponse<T> ok(T result) {
        return new CustomResponse<>(
                true,
                GeneralSuccessCode.OK.getCode(),
                GeneralSuccessCode.OK.getMessage(),
                result
        );
    }

    // 201 CREATED
    public static <T> CustomResponse<T> created(T result) {
        return new CustomResponse<>(
                true,
                GeneralSuccessCode.CREATED.getCode(),
                GeneralSuccessCode.CREATED.getMessage(),
                result
        );
    }

    // 실패 응답 (데이터 없음)
    public static <T> CustomResponse<T> onFailure(BaseErrorCode code) {
        return new CustomResponse<>(
                false,
                code.getCode(),
                code.getMessage(),
                null
        );
    }
}