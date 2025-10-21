package com.example.travel.common.interceptor;

import com.example.travel.config.RateLimitConfig.SimpleRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limiting을 위한 인터셉터입니다.
 * 요청 빈도를 제한하여 서버를 보호합니다.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final SimpleRateLimiter rateLimiter;

    @Value("${rate.limit.ms}")
    private String rateLimitMs;

    @Value("${rate.limit.api.count}")
    private String rateLimitApiCount;

    @Value("${rate.limit.auth.count}")
    private String rateLimitAuthCount;


    @Autowired
    public RateLimitInterceptor(SimpleRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response, @NonNull Object handler) {
        String requestURI = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        String rateLimitKey = clientIp + ":" + requestURI;
        String xRateLimitLimit = "X-RateLimit-Limit";
        String xRateLimitRemaining = "X-RateLimit-Remaining";
        String retryAfter = "Retry-After";

        // 인증 관련 엔드포인트는 더 엄격한 제한 적용
        if (requestURI.startsWith("/api/auth/")) {
            if (!rateLimiter.tryConsume(rateLimitKey, true)) {
                log.warn("Rate limit exceeded for auth endpoint: {} from IP: {}", requestURI,
                    clientIp);
                response.setStatus(429); // Too Many Requests
                response.setHeader(xRateLimitLimit, rateLimitAuthCount);
                response.setHeader(xRateLimitRemaining, "0");
                response.setHeader(retryAfter, rateLimitMs);
                return false;
            }

            // 남은 요청 수 헤더 추가
            response.setHeader(xRateLimitLimit, rateLimitAuthCount);
            response.setHeader(xRateLimitRemaining,
                String.valueOf(rateLimiter.getRemainingRequests(rateLimitKey, true)));
        } else if (requestURI.startsWith("/api/")) {
            if (!rateLimiter.tryConsume(rateLimitKey, false)) {
                log.warn("Rate limit exceeded for API endpoint: {} from IP: {}", requestURI,
                    clientIp);
                response.setStatus(429); // Too Many Requests
                response.setHeader(xRateLimitLimit, rateLimitApiCount);
                response.setHeader(xRateLimitRemaining, "0");
                response.setHeader(retryAfter, rateLimitMs);
                return false;
            }

            // 남은 요청 수 헤더 추가
            response.setHeader(xRateLimitLimit, rateLimitApiCount);
            response.setHeader("X-RateLimit-Remaining",
                String.valueOf(rateLimiter.getRemainingRequests(rateLimitKey, false)));
        }

        return true;
    }

    /**
     * 클라이언트 IP 주소를 가져옵니다.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

