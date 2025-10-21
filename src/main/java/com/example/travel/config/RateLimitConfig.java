package com.example.travel.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Rate Limiting 설정을 위한 구성 클래스입니다.
 * 메모리 기반 간단한 Rate Limiting을 구현합니다.
 */
@Configuration
public class RateLimitConfig {

    /**
     * 메모리 기반 간단한 Rate Limiter 구현체
     */
    @Component
    public static class SimpleRateLimiter {

        private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> lastResetTimes = new ConcurrentHashMap<>();

        // 일반 API: 분당 100회
        private static final int API_LIMIT = 10;
        private static final long API_WINDOW_MS = 60 * 1000; // 1분

        // 인증 API: 분당 10회
        private static final int AUTH_LIMIT = 100;
        private static final long AUTH_WINDOW_MS = 60 * 1000; // 1분

        public boolean tryConsume(String key, boolean isAuthEndpoint) {
            long currentTime = System.currentTimeMillis();
            long window = isAuthEndpoint ? AUTH_WINDOW_MS : API_WINDOW_MS;
            int limit = isAuthEndpoint ? AUTH_LIMIT : API_LIMIT;

            requestCounts.putIfAbsent(key, new AtomicInteger(0));
            lastResetTimes.putIfAbsent(key, new AtomicLong(currentTime));

            AtomicInteger count = requestCounts.get(key);
            AtomicLong lastReset = lastResetTimes.get(key);

            // 시간 윈도우가 지났으면 카운트 리셋
            if (currentTime - lastReset.get() > window) {
                count.set(0);
                lastReset.set(currentTime);
            }

            // 제한 확인
            if (count.get() >= limit) {
                return false;
            }

            count.incrementAndGet();
            return true;
        }

        public int getRemainingRequests(String key, boolean isAuthEndpoint) {
            long currentTime = System.currentTimeMillis();
            long window = isAuthEndpoint ? AUTH_WINDOW_MS : API_WINDOW_MS;
            int limit = isAuthEndpoint ? AUTH_LIMIT : API_LIMIT;

            AtomicInteger count = requestCounts.get(key);
            AtomicLong lastReset = lastResetTimes.get(key);

            if (count == null || lastReset == null) {
                return limit;
            }

            // 시간 윈도우가 지났으면 카운트 리셋
            if (currentTime - lastReset.get() > window) {
                return limit;
            }

            return Math.max(0, limit - count.get());
        }
    }
}

