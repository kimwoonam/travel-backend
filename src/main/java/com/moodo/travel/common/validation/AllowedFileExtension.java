package com.moodo.travel.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 허용된 파일 확장자를 검증하는 커스텀 어노테이션입니다.
 */
@Documented
@Constraint(validatedBy = AllowedFileExtensionValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedFileExtension {
    String message() default "허용되지 않은 파일 확장자입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    String[] extensions() default {};
}
