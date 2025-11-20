package com.moodo.travel.integration;

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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Account 관련 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Account 통합 테스트")
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 계정 생성
        AccountDto.SignupRequest signupRequest = new AccountDto.SignupRequest();
        signupRequest.email = "integration@test.com";
        signupRequest.password = "Password123!";
        signupRequest.name = "통합테스트";

        var signupResult = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = signupResult.getResponse().getContentAsString();
        var response = objectMapper.readValue(responseBody, AccountDto.LoginResponse.class);
        testToken = response.token;
    }

    @Test
    @DisplayName("회원가입 -> 로그인 -> 로그아웃 전체 플로우 테스트")
    void fullFlow_Signup_Login_Logout() throws Exception {
        // 1. 회원가입
        AccountDto.SignupRequest signupRequest = new AccountDto.SignupRequest();
        signupRequest.email = "newuser@test.com";
        signupRequest.password = "Password123!";
        signupRequest.name = "새사용자";

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("newuser@test.com"))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(cookie().exists("travel-jwt"));

        // 2. 로그인
        AccountDto.LoginRequest loginRequest = new AccountDto.LoginRequest();
        loginRequest.email = "newuser@test.com";
        loginRequest.password = "Password123!";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("newuser@test.com"))
            .andExpect(jsonPath("$.token").exists());

        // 3. 로그아웃
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void signup_Fail_DuplicateEmail() throws Exception {
        AccountDto.SignupRequest request = new AccountDto.SignupRequest();
        request.email = "integration@test.com"; // 이미 존재하는 이메일
        request.password = "Password123!";
        request.name = "중복테스트";

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void login_Fail_WrongPassword() throws Exception {
        AccountDto.LoginRequest request = new AccountDto.LoginRequest();
        request.email = "integration@test.com";
        request.password = "WrongPassword123!";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("계정 삭제 성공")
    void deleteAccount_Success() throws Exception {
        mockMvc.perform(delete("/api/auth/delete")
                .header("Authorization", "Bearer " + testToken)
                .param("email", "integration@test.com")
                .param("password", "Password123!"))
            .andExpect(status().isNoContent());
    }
}
