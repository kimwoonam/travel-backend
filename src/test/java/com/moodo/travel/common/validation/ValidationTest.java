package com.moodo.travel.common.validation;

import com.moodo.travel.common.util.ValidationUtil;
import jakarta.validation.Payload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.annotation.Annotation;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 검증 로직을 테스트하는 클래스입니다.
 */
class ValidationTest {

    @Test
    @DisplayName("비밀번호 강도 검증 테스트")
    void testPasswordStrength() {
        // Valid password
        PasswordStrengthValidator validator = new PasswordStrengthValidator();
        
        // 유효한 비밀번호
        assertThat(validator.isValid("Password123!", null)).isTrue();
        assertThat(validator.isValid("MyPass1@", null)).isTrue();
        
        // 유효하지 않은 비밀번호
        assertThat(validator.isValid("password", null)).isFalse(); // 대문자, 숫자, 특수문자 없음
        assertThat(validator.isValid("PASSWORD123!", null)).isFalse(); // 소문자 없음
        assertThat(validator.isValid("Password!", null)).isFalse(); // 숫자 없음
        assertThat(validator.isValid("Password123", null)).isFalse(); // 특수문자 없음
        assertThat(validator.isValid("Pass1!", null)).isFalse(); // 8자 미만
        assertThat(validator.isValid(null, null)).isFalse(); // null
        assertThat(validator.isValid("", null)).isFalse(); // 빈 문자열
    }

    @Test
    @DisplayName("한국어 이름 검증 테스트")
    void testKoreanName() {
        KoreanNameValidator validator = new KoreanNameValidator();
        
        // 유효한 이름
        assertThat(validator.isValid("홍길동", null)).isTrue();
        assertThat(validator.isValid("김철수", null)).isTrue();
        assertThat(validator.isValid("John", null)).isTrue();
        assertThat(validator.isValid("Alice", null)).isTrue();
        assertThat(validator.isValid("김", null)).isFalse(); // 2자 미만
        
        // 유효하지 않은 이름
        assertThat(validator.isValid("홍123", null)).isFalse(); // 숫자 포함
        assertThat(validator.isValid("홍@길동", null)).isFalse(); // 특수문자 포함
        assertThat(validator.isValid("홍길동홍길동홍길동홍길동홍길동홍길동", null)).isFalse(); // 10자 초과
        assertThat(validator.isValid("JohnSmithJohnSmithJohnSmith", null)).isFalse(); // 20자 초과
        assertThat(validator.isValid(null, null)).isFalse(); // null
        assertThat(validator.isValid("", null)).isFalse(); // 빈 문자열
    }

    @Test
    @DisplayName("파일 확장자 검증 테스트")
    void testFileExtension() {
        AllowedFileExtensionValidator validator = new AllowedFileExtensionValidator();
        validator.initialize(new AllowedFileExtension() {
            @Override
            public Class<? extends Annotation> annotationType() { return AllowedFileExtension.class; }
            @Override
            public String message() { return ""; }
            @Override
            public Class<?>[] groups() { return new Class[0]; }
            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends Payload>[] payload() { return new Class[0]; }
            @Override
            public String[] extensions() { return new String[]{"jpg", "png", "pdf", "txt"}; }
        });
        
        // 유효한 파일명
        assertThat(validator.isValid("test.jpg", null)).isTrue();
        assertThat(validator.isValid("document.pdf", null)).isTrue();
        assertThat(validator.isValid("image.PNG", null)).isTrue(); // 대소문자 구분 없음
        
        // 유효하지 않은 파일명
        assertThat(validator.isValid("test.exe", null)).isFalse(); // 허용되지 않은 확장자
        assertThat(validator.isValid("test", null)).isFalse(); // 확장자 없음
        assertThat(validator.isValid("test.", null)).isFalse(); // 확장자 없음
    }

    @Test
    @DisplayName("ValidationUtil 테스트")
    void testValidationUtil() {
        // SQL 인젝션 검사
        assertThat(ValidationUtil.containsSqlInjection("SELECT * FROM users")).isTrue();
        assertThat(ValidationUtil.containsSqlInjection("DROP TABLE users")).isTrue();
        assertThat(ValidationUtil.containsSqlInjection("normal text")).isFalse();
        
        // XSS 검사
        assertThat(ValidationUtil.containsXss("<script>alert('xss')</script>")).isTrue();
        assertThat(ValidationUtil.containsXss("<iframe src='evil.com'></iframe>")).isTrue();
        assertThat(ValidationUtil.containsXss("normal text")).isFalse();
        
        // 파일명 안전성 검사
        assertThat(ValidationUtil.isUnsafeFilename("file<name.txt")).isTrue();
        assertThat(ValidationUtil.isUnsafeFilename("file:name.txt")).isTrue();
        assertThat(ValidationUtil.isUnsafeFilename("file name.txt")).isFalse();
        
        // 길이 검사
        assertThat(ValidationUtil.isValidLength("test", 10)).isTrue();
        assertThat(ValidationUtil.isValidLength("test", 2, 10)).isTrue();
        assertThat(ValidationUtil.isValidLength("test", 2, 3)).isFalse();
        
        // 이메일 검사
        assertThat(ValidationUtil.isValidEmail("test@example.com")).isTrue();
        assertThat(ValidationUtil.isValidEmail("invalid-email")).isFalse();
        
        // 파일 크기 검사
        assertThat(ValidationUtil.isValidFileSize(1024, 2048)).isTrue();
        assertThat(ValidationUtil.isValidFileSize(3000, 2048)).isFalse();
        
        // 입력값 정리
        assertThat(ValidationUtil.sanitizeInput("<script>alert('xss')</script>"))
            .isEqualTo("alert('xss')");
        assertThat(ValidationUtil.sanitizeInput("test    multiple    spaces"))
            .isEqualTo("test multiple spaces");
    }
}
