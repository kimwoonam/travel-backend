package com.example.travel.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountDto {

    public static class SignupRequest {

        @Email
        @NotBlank
        public String email;

        @NotBlank
        @Size(min = 6)
        public String password;

        @NotBlank
        public String name;
    }

    public static class LoginRequest {

        @Email
        @NotBlank
        public String email;

        @NotBlank
        public String password;
    }

    public static class LoginResponse {

        public String token;
        public String email;
        public String name;

        public LoginResponse(String token, String email, String name) {
            this.token = token;
            this.email = email;
            this.name = name;
        }
    }
}
