package com.example.travel.common.util;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * 유틸리티 클래스 RandomGeneratorUtil은 무작위 문자열을 생성하는 기능을 제공합니다.
 * 보안상 더 강력한 무작위성을 제공하기 위해 SecureRandom을 활용합니다.
 */
@Component
public class RandomGeneratorUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 지정된 길이의 무작위 영문 및 숫자로 구성된 문자열을 생성합니다.
     *
     * @param length 생성할 문자열의 길이
     * @return 무작위로 생성된 문자열
     */
    public static String generateRandomString(int length) {
        // 입력된 길이가 0보다 작으면 예외를 발생시킵니다.
        if (length < 1) {
            throw new IllegalArgumentException("문자열의 길이는 1 이상이어야 합니다.");
        }

        // 보안상 더 강력한 무작위성을 제공하는 SecureRandom을 사용합니다.
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            // CHARACTERS 문자열에서 무작위 인덱스의 문자를 선택합니다.
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
