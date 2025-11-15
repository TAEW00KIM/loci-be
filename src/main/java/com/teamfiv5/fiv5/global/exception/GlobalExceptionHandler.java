package com.teamfiv5.fiv5.global.exception;

import com.teamfiv5.fiv5.global.exception.code.BaseErrorCode;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.global.response.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomResponse<Void>> handleCustomException(CustomException ex) {
        BaseErrorCode code = ex.getCode();
        log.warn("[CustomException]: {}", code.getMessage());

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(CustomResponse.onFailure(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Void>> handleAllException(Exception ex) {
        log.error("[Exception]: {}", ex.getMessage(), ex);

        BaseErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CustomResponse.onFailure(errorCode));
    }
}