package com.teamfiv5.fiv5.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.global.response.CustomResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 401 에러 코드를 ErrorCode에서 가져옵니다.
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // 응답 상태를 401 (Unauthorized)로 설정
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 응답 본문을 CustomResponse 형식으로 작성
        String jsonResponse = objectMapper.writeValueAsString(CustomResponse.onFailure(errorCode));
        response.getWriter().write(jsonResponse);
    }
}