package com.example.travel.user;

import com.example.travel.common.provider.JwtProvider;
import com.example.travel.common.provider.RedisProvider;
import com.example.travel.user.dto.AuthDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;

    @Autowired
    public UserService(UserRepository userRepository, JwtProvider jwtProvider,
        RedisProvider redisProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.redisProvider = redisProvider;
    }

    @Transactional
    public AuthDtos.LoginResponse signup(AuthDtos.SignupRequest req) {
        userRepository.findByEmail(req.email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });
        User user = new User();
        user.setEmail(req.email);
        user.setDisplayName(req.displayName);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user = userRepository.save(user);

        String token = jwtProvider.generateToken(user.getEmail(), user.getDisplayName());
        userRepository.save(user);

        return new AuthDtos.LoginResponse(token, user.getEmail(), user.getDisplayName());
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByEmail(req.email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(user.getEmail(), user.getDisplayName());
        redisProvider.setJwt(token);
        return new AuthDtos.LoginResponse(token, user.getEmail(), user.getDisplayName());
    }

    @Transactional
    public void deleteByEmail(String token, String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        userRepository.delete(user);
        redisProvider.removeJwt(token);
    }

     public void logout(String token) {
        try {
            User user = userRepository.findByEmail(jwtProvider.getEmail(token))
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
