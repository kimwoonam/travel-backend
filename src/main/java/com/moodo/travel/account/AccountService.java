package com.moodo.travel.account;

import com.moodo.travel.account.dto.AccountDto;
import com.moodo.travel.account.dto.AccountDto.LoginResponse;
import com.moodo.travel.account.dto.AccountDto.SignupRequest;
import com.moodo.travel.common.provider.JwtProvider;
import com.moodo.travel.common.provider.RedisProvider;
import com.moodo.travel.common.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가입, 로그인, 계정 삭제, 로그아웃, 쿠키 생성 등 계정 관련 작업을 처리하는 서비스 클래스입니다.
 * Redis를 통해 저장소 계층, 토큰 생성, 세션 관리와 상호 작용합니다.
 */
@Service
public class AccountService {

    private static final Logger log = LogManager.getLogger(AccountService.class);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final AccountRepository accountRepository;
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;

    @Autowired
    public AccountService(AccountRepository accountRepository, JwtProvider jwtProvider,
        RedisProvider redisProvider) {
        this.accountRepository = accountRepository;
        this.jwtProvider = jwtProvider;
        this.redisProvider = redisProvider;
    }

    /**
     * 제공된 가입 요청 정보를 사용하여 새 계정을 등록하고 로그인 응답을 반환합니다..
     * 이메일이 이미 등록되어 있는지 확인하고, 그렇지 않은 경우 계정 생성,
     * 토큰 생성 및 Redis 캐시에 토큰 저장을 처리합니다.
     *
     * @param req 계정 생성을 위한 이메일, 비밀번호, 이름을 포함하는 가입 요청 객체
     * @return 새로 생성된 계정의 생성된 토큰, 이메일 및 이름을 포함하는 LoginResponse 객체를 반환
     * @throws IllegalArgumentException 이메일이 이미 등록되어 있는 경우 발생
     */
    @Transactional
    public LoginResponse signup(SignupRequest req) {

        String email = ValidationUtil.sanitizeInput(req.email);

        accountRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });

        Account account = new Account();
        account.setUuid(UUID.randomUUID().toString());
        account.setEmail(req.email);
        account.setName(req.name);
        account.setPasswordHash(passwordEncoder.encode(req.password));
        account = accountRepository.save(account);

        String token = jwtProvider.generateToken(account.getEmail(), account.getName());
        redisProvider.setJwt(token);
        redisProvider.setUserInfo(account);
        accountRepository.save(account);

        return new LoginResponse(token, account.getEmail(), account.getName());
    }

    /**
     * 로그인 자격 증명을 기반으로 사용자를 인증하고 로그인 응답을 반환합니다.
     * 이 메서드는 사용자의 이메일과 비밀번호를 검증하고, 인증 성공 시 JWT 토큰을 생성하며,
     * 세션 관리를 위해 토큰을 Redis 캐시에 저장합니다.
     * <br/>
     * @param req 사용자의 이메일과 비밀번호를 포함하는 로그인 요청 객체
     * @return 생성된 토큰, 사용자의 이메일, 이름을 포함하는 LoginResponse 객체를 반환
     * @throws IllegalArgumentException 이메일을 찾을 수 없거나 비밀번호가 저장된 해시와 일치하지 않으면 발생
     */
    public LoginResponse login(AccountDto.LoginRequest req) {
        Account account = accountRepository.findByEmail(req.email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(account.getEmail(), account.getName());
        redisProvider.setJwt(token);
        redisProvider.setUserInfo(account);
        return new LoginResponse(token, account.getEmail(), account.getName());
    }

    /**
     * 제공된 비밀번호가 저장된 비밀번호 해시와 일치하는 경우, 제공된 이메일로 식별된 계정을 삭제합니다.
     * 또한, Redis 캐시에서 연결된 JWT 토큰을 제거합니다.
     *
     * @param token 세션 관리에 사용되는 사용자와 연관된 JWT 토큰
     * @param email 삭제할 계정의 이메일 주소
     * @param password 계정 비밀번호, 검증에 필요
     * @throws IllegalArgumentException 지정된 이메일 계정이 존재하지 않거나 제공된 비밀번호가 유효하지 않은 경우 발생
     */
    @Transactional
    public void deleteByEmail(String token, String email, String password) {
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        redisProvider.removeUserInfo(account.getEmail());
        redisProvider.removeJwt(token);
        accountRepository.delete(account);
    }

    /**
     * 제공된 JWT 토큰을 무효화하여 로그아웃 프로세스를 실행합니다.
     * Redis 캐시에서 토큰을 제거하여 관련 세션을 종료합니다.
     *
     * @param token 세션을 식별하여 로그아웃하는 데 사용되는 JWT 토큰
     * @throws IllegalArgumentException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우 반환
     */
    public void logout(String token) {
        try {
            Account account = accountRepository.findByEmail(jwtProvider.getEmail(token))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            redisProvider.removeUserInfo(account.getEmail());
            redisProvider.removeJwt(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    /**
     * Retrieves an account associated with the given email address.
     * If no account is found, an exception is thrown.
     *
     * @param email the email address used to identify the account
     * @return the account associated with the specified email
     * @throws IllegalArgumentException if no account is found for the given email
     */
    public Account getAccountByEmail(String email) {
        return redisProvider.getUserInfo(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * 주어진 HTTP 서블릿 요청에서 이메일 속성을 가져옵니다.
     * 이메일 속성이 null이면 RuntimeException이 발생합니다.
     *
     * @param request 이메일 속성을 포함하는 HttpServletRequest 객체
     * @return 문자열로 된 이메일 속성
     * @throws RuntimeException 이메일 속성이 null인 경우
     */
    public String getEmail(HttpServletRequest request) throws RuntimeException {

        if (Objects.isNull(request.getAttribute("email"))) {
            log.error("email not null");
            throw new RuntimeException("email is null");
        }

        return request.getAttribute("email").toString();
    }

    /**
     * 지정된 토큰 값으로 HTTP 쿠키를 생성합니다. 이 쿠키는 최대 3600초의 유효 기간, "/" 경로,
     * HTTP 전용 액세스, 보안 플래그 없음, "엄격한" SameSite 정책 등의 속성으로 구성됩니다.
     *
     * @param token 쿠키에 저장될 토큰 문자열
     * @return 생성된 쿠키를 나타내는 ResponseCookie 객체를 반환
     */
    public ResponseCookie createCookie(String token) {
        return ResponseCookie.from("travel-jwt", token)
            .maxAge(3600)
            .path("/")
            .httpOnly(true)
            .secure(false)
            .sameSite("Strict")
            .build();
    }
}
