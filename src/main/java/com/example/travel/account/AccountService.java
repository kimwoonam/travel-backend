package com.example.travel.account;

import com.example.travel.common.provider.JwtProvider;
import com.example.travel.common.provider.RedisProvider;
import com.example.travel.account.dto.AccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;

    @Autowired
    public AccountService(AccountRepository accountRepository, JwtProvider jwtProvider,
        RedisProvider redisProvider) {
        this.accountRepository = accountRepository;
        this.jwtProvider = jwtProvider;
        this.redisProvider = redisProvider;
    }

    @Transactional
    public AccountDto.LoginResponse signup(AccountDto.SignupRequest req) {
        accountRepository.findByEmail(req.email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });
        Account account = new Account();
        account.setEmail(req.email);
        account.setName(req.name);
        account.setPasswordHash(passwordEncoder.encode(req.password));
        account = accountRepository.save(account);

        String token = jwtProvider.generateToken(account.getEmail(), account.getName());
        redisProvider.setJwt(token);
        accountRepository.save(account);

        return new AccountDto.LoginResponse(token, account.getEmail(), account.getName());
    }

    public AccountDto.LoginResponse login(AccountDto.LoginRequest req) {
        Account account = accountRepository.findByEmail(req.email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(account.getEmail(), account.getName());
        redisProvider.setJwt(token);
        return new AccountDto.LoginResponse(token, account.getEmail(), account.getName());
    }

    @Transactional
    public void deleteByEmail(String token, String email, String password) {
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        accountRepository.delete(account);
        redisProvider.removeJwt(token);
    }

     public void logout(String token) {
        try {
            Account account = accountRepository.findByEmail(jwtProvider.getEmail(token))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            redisProvider.removeJwt(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

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
