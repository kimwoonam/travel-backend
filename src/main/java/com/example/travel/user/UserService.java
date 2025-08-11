package com.example.travel.user;

import com.example.travel.config.JwtUtil;
import com.example.travel.user.dto.AuthDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
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
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getDisplayName());
        user.setJwtToken(token);
        userRepository.save(user);
        
        return new AuthDtos.LoginResponse(token, user.getEmail(), user.getDisplayName());
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByEmail(req.email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getDisplayName());
        user.setJwtToken(token);
        userRepository.save(user);
        
        return new AuthDtos.LoginResponse(token, user.getEmail(), user.getDisplayName());
    }

    @Transactional
    public void deleteByEmail(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        userRepository.delete(user);
    }

    @Transactional
    public void logout(String token) {
        try {
            JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
            User user = userRepository.findByEmail(tokenInfo.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // JWT 토큰을 null로 설정하여 무효화
            user.setJwtToken(null);
            userRepository.save(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}
