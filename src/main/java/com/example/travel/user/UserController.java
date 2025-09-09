package com.example.travel.user;

import com.example.travel.user.dto.AuthDtos;
import jakarta.validation.Valid;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthDtos.SignupRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        try {

            AuthDtos.LoginResponse response = userService.login(request);
            ResponseCookie cookie = userService.createCookie(response.token);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));

            return new ResponseEntity<>(response, headers, HttpStatus.OK);
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
                userService.deleteByEmail(token, email, password);

                ResponseCookie cookie = userService.createCookie("");
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));

                return new ResponseEntity<>(null, headers, HttpStatus.NO_CONTENT);
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
                userService.logout(token);

                ResponseCookie cookie = userService.createCookie("");
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));
                return new ResponseEntity<>(null, headers, HttpStatus.NO_CONTENT);
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 토큰");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("로그아웃 중 오류가 발생했습니다");
        }
    }
}
