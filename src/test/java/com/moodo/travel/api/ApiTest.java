package com.moodo.travel.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodo.travel.account.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API 엔드포인트 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API 테스트")
class ApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 계정 생성 및 로그인
        AccountDto.SignupRequest signupRequest = new AccountDto.SignupRequest();
        signupRequest.email = "apitest@test.com";
        signupRequest.password = "Password123!";
        signupRequest.name = "API테스트";

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        AccountDto.LoginRequest loginRequest = new AccountDto.LoginRequest();
        loginRequest.email = "apitest@test.com";
        loginRequest.password = "Password123!";

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AccountDto.LoginResponse response = objectMapper.readValue(responseBody, AccountDto.LoginResponse.class);
        authToken = response.token;
    }

    @Test
    @DisplayName("인증 없이 보호된 API 접근 시 401 반환")
    void accessProtectedApi_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/board"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 사용자가 보호된 API 접근 성공")
    void accessProtectedApi_WithAuth_Success() throws Exception {
        mockMvc.perform(get("/api/board")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 토큰으로 API 접근 시 401 반환")
    void accessProtectedApi_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/board")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원가입 API - 유효성 검사 실패")
    void signup_ValidationFailed() throws Exception {
        AccountDto.SignupRequest request = new AccountDto.SignupRequest();
        request.email = "invalid-email"; // 잘못된 이메일 형식
        request.password = "123"; // 너무 짧은 비밀번호
        request.name = ""; // 빈 이름

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 API - 유효성 검사 실패")
    void login_ValidationFailed() throws Exception {
        AccountDto.LoginRequest request = new AccountDto.LoginRequest();
        request.email = "invalid-email";
        request.password = "";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CORS 헤더 확인")
    void corsHeaders_Present() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rate Limiting 헤더 확인")
    void rateLimitHeaders_Present() throws Exception {
        mockMvc.perform(get("/api/board")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(header().exists("X-RateLimit-Limit"))
            .andExpect(header().exists("X-RateLimit-Remaining"));
    }
}
