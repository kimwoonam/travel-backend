package com.moodo.travel.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 비밀번호 강도를 검증하는 Validator입니다.
 */
public class PasswordStrengthValidator implements ConstraintValidator<PasswordStrength, String> {

    // 최소 8자, 대소문자, 숫자, 특수문자 포함
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Override
    public void initialize(PasswordStrength constraintAnnotation) {
        // 초기화 로직이 필요한 경우 여기에 구현
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
