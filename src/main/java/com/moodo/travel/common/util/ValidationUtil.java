package com.moodo.travel.common.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * 입력값 검증을 위한 유틸리티 클래스입니다.
 */
@Component
public class ValidationUtil {

    // SQL 인젝션 방지를 위한 패턴
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(union|select|insert|update|delete|drop|create|alter|exec|execute|script).*",
        Pattern.CASE_INSENSITIVE
    );

    // XSS 방지를 위한 패턴
    private static final Pattern XSS_PATTERN = Pattern.compile(
        ".*(<script|</script|<iframe|</iframe|javascript:|onload=|onerror=).*",
        Pattern.CASE_INSENSITIVE
    );

    // 파일명 안전성 검사 패턴
    private static final Pattern UNSAFE_FILENAME_PATTERN = Pattern.compile(
        ".*[<>:\"/\\\\|?*].*"
    );

    /**
     * SQL 인젝션 공격을 방지하기 위한 검증
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).matches();
    }

    /**
     * XSS 공격을 방지하기 위한 검증
     */
    public static boolean containsXss(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).matches();
    }

    /**
     * 안전하지 않은 파일명인지 검증
     */
    public static boolean isUnsafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return true;
        }
        return UNSAFE_FILENAME_PATTERN.matcher(filename).matches();
    }

    /**
     * 입력값 길이 검증
     */
    public static boolean isValidLength(String input, int maxLength) {
        if (input == null) {
            return true; // null은 허용
        }
        return input.length() <= maxLength;
    }

    /**
     * 입력값 길이 검증 (최소, 최대 길이)
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return minLength == 0; // null은 최소 길이가 0일 때만 허용
        }
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 빈 문자열 또는 공백만 있는지 검증
     */
    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * 이메일 형식 검증 (기본 Bean Validation보다 엄격)
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

    /**
     * 파일 크기 검증 (바이트 단위)
     */
    public static boolean isValidFileSize(long fileSize, long maxSizeInBytes) {
        return fileSize > 0 && fileSize <= maxSizeInBytes;
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 변환
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 입력값에서 특수문자 제거
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // HTML 태그 제거
        String sanitized = input.replaceAll("<[^>]*>", "");
        
        // SQL 인젝션 위험 문자 제거
        sanitized = sanitized.replaceAll("['\"`;]", "");
        
        // 연속된 공백을 하나로 변경
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        return sanitized.trim();
    }
}
