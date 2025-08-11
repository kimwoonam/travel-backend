package com.example.travel.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public static class SignupRequest {
        @Email
        @NotBlank
        public String email;

        @NotBlank
        @Size(min = 6)
        public String password;

        @NotBlank
        public String displayName;
    }

    public static class LoginRequest {
        @Email
        @NotBlank
        public String email;

        @NotBlank
        public String password;
    }

    public static class UserResponse {
        public Long id;
        public String email;
        public String displayName;
        public String token;
        
        public UserResponse(Long id, String email, String displayName, String token) {
            this.id = id;
            this.email = email;
            this.displayName = displayName;
            this.token = token;
        }
    }

    public static class LoginResponse {
        public String token;
        public String email;
        public String displayName;
        
        public LoginResponse(String token, String email, String displayName) {
            this.token = token;
            this.email = email;
            this.displayName = displayName;
        }
    }
}
