package com.example.travel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Spring Security 설정 클래스입니다.
 * 보안 헤더, CSRF 보호, HTTPS 강제 등을 설정합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호 인코더를 설정합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 보안 필터 체인을 설정합니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 활성화 (API 서버이므로 일부 엔드포인트에서만)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/auth/signup", "/api/auth/login")
                .csrfTokenRepository(
                    new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository())
            )

            // 세션 관리 설정 (STATELESS로 JWT 사용)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 보안 헤더 설정
            .headers(headers -> headers
                // XSS 보호
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                // 콘텐츠 타입 스니핑 방지
                .contentTypeOptions(contentType -> {
                })
                // HTTP Strict Transport Security (HTTPS 강제)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .preload(true)
                )
                // 프레임 옵션 (클릭재킹 방지)
                .frameOptions(FrameOptionsConfig::deny)
                // 참조자 정책
                // .referrerPolicy(Customizer.withDefaults())
                .referrerPolicy(referrerPolicy -> referrerPolicy.policy(
                    ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

            )

            // CORS 설정
            .cors(cors -> {
            })

            // HTTP Basic 인증 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)

            // 폼 로그인 비활성화
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
