package com.example.travel.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 한국어 이름을 검증하는 Validator입니다.
 */
public class KoreanNameValidator implements ConstraintValidator<KoreanName, String> {

    // 한글 2-10자 또는 영문 2-20자
    private static final Pattern KOREAN_NAME_PATTERN = Pattern.compile(
        "^([가-힣]{2,10}|[a-zA-Z]{2,20})$"
    );

    @Override
    public void initialize(KoreanName constraintAnnotation) {
        // 초기화 로직이 필요한 경우 여기에 구현
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return KOREAN_NAME_PATTERN.matcher(name.trim()).matches();
    }
}
