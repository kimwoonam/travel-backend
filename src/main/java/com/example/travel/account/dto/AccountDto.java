package com.example.travel.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AccountDto 클래스는 사용자와 관련된 데이터 전송 객체를 정의합니다.
 * 이 클래스는 주로 사용자 가입, 로그인 요청/응답 모델을 포함하며,
 * 사용자 인증 및 계정 관리와 관련된 데이터를 처리하는 데 사용됩니다.
 */
public class AccountDto {

    /**
     * 사용자 가입을 위한 요청 객체를 나타냅니다. 이 클래스는 새 사용자 계정을 생성하는 데 필요한 정보를 수집하는 데 사용됩니다.
     * 필드에는 이메일, 비밀번호, 이름이 포함되며
     * 제공된 데이터의 정확성과 완전성을 보장하기 위한 유효성 검사 제약 조건이 있습니다.
     */
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

    /**
     * 사용자 로그인에 사용되는 요청 객체를 나타냅니다.
     * 이 클래스는 사용자 인증에 필요한 정보를 수집합니다.
     * 필드에는 이메일과 비밀번호가 포함되며, 제약 조건을 사용하여 유효성이 검사됩니다.
     * 이 요청은 일반적으로 인증과 관련된 API 엔드포인트에서 사용
     */
    public static class LoginRequest {

        @Email
        @NotBlank
        public String email;

        @NotBlank
        public String password;
    }

    /**
     * 성공적인 로그인 작업 후 반환되는 응답을 나타냅니다.
     * LoginResponse에는 필수 사용자 정보와 후속 요청 인증에 사용되는 토큰이 포함되어 있습니다
     * <br/>
     * 이 클래스는 일반적으로 API 응답 컨텍스트에서 사용되며, 인증된 사용자의 토큰, 이메일, 이름을 캡슐화합니다.
     */
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
