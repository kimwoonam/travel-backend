package com.example.travel.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * 허용된 파일 확장자를 검증하는 Validator입니다.
 */
public class AllowedFileExtensionValidator implements ConstraintValidator<AllowedFileExtension, String> {

    private List<String> allowedExtensions;

    @Override
    public void initialize(AllowedFileExtension constraintAnnotation) {
        allowedExtensions = Arrays.asList(constraintAnnotation.extensions());
    }

    @Override
    public boolean isValid(String filename, ConstraintValidatorContext context) {
        if (filename == null || filename.trim().isEmpty()) {
            return true; // null이나 빈 문자열은 허용 (선택적 파일)
        }

        if (allowedExtensions.isEmpty()) {
            return true; // 허용된 확장자가 설정되지 않은 경우 허용
        }

        String extension = getFileExtension(filename);
        if (extension == null) {
            return false; // 확장자가 없는 경우 거부
        }

        return allowedExtensions.contains(extension.toLowerCase());
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1);
    }
}
