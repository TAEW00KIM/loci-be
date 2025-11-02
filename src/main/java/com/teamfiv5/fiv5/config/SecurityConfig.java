package com.teamfiv5.fiv5.config;

import com.teamfiv5.fiv5.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] SWAGGER_URL_PATTERNS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ... (csrf, httpBasic, formLogin, sessionManagement 비활성화) ...
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // (2) 경로별 권한 설정 (수정)
        http
                .authorizeHttpRequests(auth -> auth
                        // /health, /api/v1/auth/**, Swagger 경로는 인증 없이 허용
                        .requestMatchers(
                                "/health",
                                "/api/v1/auth/**"
                        ).permitAll()
                        .requestMatchers(SWAGGER_URL_PATTERNS).permitAll() // (추가)
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated());
        // (3) JWT 필터 추가 (유지)
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}