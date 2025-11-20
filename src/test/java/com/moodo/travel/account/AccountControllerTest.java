package com.moodo.travel.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodo.travel.account.dto.AccountDto;
import com.moodo.travel.account.dto.AccountDto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AccountController 단위 테스트
 */
@WebMvcTest(AccountController.class)
@DisplayName("AccountController 테스트")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() throws Exception {
        // given
        AccountDto.SignupRequest request = new AccountDto.SignupRequest();
        request.email = "test@example.com";
        request.password = "Password123!";
        request.name = "테스트";

        LoginResponse response = new LoginResponse("test-token", "test@example.com", "테스트");
        when(accountService.signup(any())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.token").value("test-token"))
            .andExpect(cookie().exists("travel-jwt"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 등록된 이메일")
    void signup_Fail_EmailAlreadyExists() throws Exception {
        // given
        AccountDto.SignupRequest request = new AccountDto.SignupRequest();
        request.email = "existing@example.com";
        request.password = "Password123!";
        request.name = "테스트";

        when(accountService.signup(any())).thenThrow(new IllegalArgumentException("Email already registered"));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Email already registered"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        AccountDto.LoginRequest request = new AccountDto.LoginRequest();
        request.email = "test@example.com";
        request.password = "Password123!";

        LoginResponse response = new LoginResponse("test-token", "test@example.com", "테스트");
        when(accountService.login(any())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.token").value("test-token"))
            .andExpect(cookie().exists("travel-jwt"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명")
    void login_Fail_InvalidCredentials() throws Exception {
        // given
        AccountDto.LoginRequest request = new AccountDto.LoginRequest();
        request.email = "test@example.com";
        request.password = "wrongpassword";

        when(accountService.login(any())).thenThrow(new IllegalArgumentException("Invalid credentials"));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Invalid credentials"));
    }

    @Test
    @DisplayName("계정 삭제 성공")
    void delete_Success() throws Exception {
        // given
        String token = "Bearer test-token";
        doNothing().when(accountService).deleteByEmail(anyString(), anyString(), anyString());

        // when & then
        mockMvc.perform(delete("/api/auth/delete")
                .header("Authorization", token)
                .param("email", "test@example.com")
                .param("password", "Password123!"))
            .andExpect(status().isNoContent())
            .andExpect(cookie().exists("travel-jwt"));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // given
        String token = "Bearer test-token";
        doNothing().when(accountService).logout(anyString());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", token))
            .andExpect(status().isNoContent())
            .andExpect(cookie().exists("travel-jwt"));
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효하지 않은 토큰 형식")
    void logout_Fail_InvalidTokenFormat() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "InvalidToken"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("유효하지 않은 토큰"));
    }
}
