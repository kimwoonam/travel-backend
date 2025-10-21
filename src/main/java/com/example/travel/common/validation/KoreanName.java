package com.example.travel.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 한국어 이름을 검증하는 커스텀 어노테이션입니다.
 * 한글 2-10자, 영문 2-20자, 숫자나 특수문자는 허용하지 않습니다.
 */
@Documented
@Constraint(validatedBy = KoreanNameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KoreanName {
    String message() default "이름은 한글 2-10자 또는 영문 2-20자여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
