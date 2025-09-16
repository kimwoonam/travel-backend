package com.example.travel.account;

import com.example.travel.account.dto.AccountDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AccountDto.SignupRequest request) {
        try {

            AccountDto.LoginResponse response = accountService.signup(request);
            ResponseCookie cookie = accountService.createCookie(response.token);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));

            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AccountDto.LoginRequest request) {
        try {

            AccountDto.LoginResponse response = accountService.login(request);
            ResponseCookie cookie = accountService.createCookie(response.token);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));
            return ResponseEntity.ok().headers(headers).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String authHeader,
        @RequestParam String email, @RequestParam String password) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                accountService.deleteByEmail(token, email, password);

                ResponseCookie cookie = accountService.createCookie("");
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));

                return ResponseEntity.noContent().headers(headers).build();
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 토큰");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                accountService.logout(token);

                ResponseCookie cookie = accountService.createCookie("");
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));
                return ResponseEntity.noContent().headers(headers).build();
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 토큰");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("로그아웃 중 오류가 발생했습니다");
        }
    }
}
