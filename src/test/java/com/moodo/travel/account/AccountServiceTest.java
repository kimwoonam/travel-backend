package com.moodo.travel.account;

import com.moodo.travel.account.dto.AccountDto;
import com.moodo.travel.account.dto.AccountDto.LoginResponse;
import com.moodo.travel.account.dto.AccountDto.SignupRequest;
import com.moodo.travel.common.provider.JwtProvider;
import com.moodo.travel.common.provider.RedisProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AccountService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 테스트")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisProvider redisProvider;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private SignupRequest signupRequest;
    private AccountDto.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setName("테스트");
        testAccount.setPasswordHash("$2a$10$encodedPasswordHash");
        testAccount.setUuid("test-uuid");

        signupRequest = new SignupRequest();
        signupRequest.email = "newuser@example.com";
        signupRequest.password = "Password123!";
        signupRequest.name = "새사용자";

        loginRequest = new AccountDto.LoginRequest();
        loginRequest.email = "test@example.com";
        loginRequest.password = "password123";
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("test-token");

        // when
        LoginResponse response = accountService.signup(signupRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email).isEqualTo(testAccount.getEmail());
        assertThat(response.token).isEqualTo("test-token");
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(redisProvider).setJwt("test-token");
        verify(redisProvider).setUserInfo(any(Account.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 등록된 이메일")
    void signup_Fail_EmailAlreadyExists() {
        // given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // when & then
        assertThatThrownBy(() -> accountService.signup(signupRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already registered");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        when(accountRepository.findByEmail(loginRequest.email)).thenReturn(Optional.of(testAccount));
        when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("test-token");
        // PasswordEncoder는 실제 인스턴스를 사용해야 하므로 실제로는 @Spy를 사용하거나 테스트용 설정 필요

        // when
        LoginResponse response = accountService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email).isEqualTo(testAccount.getEmail());
        verify(redisProvider).setJwt("test-token");
        verify(redisProvider).setUserInfo(any(Account.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_Fail_EmailNotFound() {
        // given
        when(accountRepository.findByEmail(loginRequest.email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.login(loginRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("계정 삭제 성공")
    void deleteByEmail_Success() {
        // given
        String token = "test-token";
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).delete(any(Account.class));

        // when
        accountService.deleteByEmail(token, testAccount.getEmail(), "password123");

        // then
        verify(redisProvider).removeUserInfo(testAccount.getEmail());
        verify(redisProvider).removeJwt(token);
        verify(accountRepository).delete(testAccount);
    }

    @Test
    @DisplayName("계정 삭제 실패 - 사용자 없음")
    void deleteByEmail_Fail_UserNotFound() {
        // given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.deleteByEmail("token", "test@example.com", "password"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String token = "test-token";
        when(jwtProvider.getEmail(token)).thenReturn(testAccount.getEmail());
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        // when
        accountService.logout(token);

        // then
        verify(redisProvider).removeUserInfo(testAccount.getEmail());
        verify(redisProvider).removeJwt(token);
    }

    @Test
    @DisplayName("쿠키 생성 테스트")
    void createCookie_Success() {
        // when
        var cookie = accountService.createCookie("test-token");

        // then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("travel-jwt");
        assertThat(cookie.getValue()).isEqualTo("test-token");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        assertThat(cookie.isHttpOnly()).isTrue();
    }
}
