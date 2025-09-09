package com.example.travel.common.interceptor;

import com.example.travel.common.provider.JwtProvider;
import com.example.travel.common.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LogManager.getLogger(JwtInterceptor.class);
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Autowired
    public JwtInterceptor(JwtProvider jwtProvider, RedisService redisService) {
        this.jwtProvider = jwtProvider;
        this.redisService = redisService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        // OPTIONS 요청은 CORS preflight 요청이므로 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 인증이 필요하지 않은 엔드포인트들
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") && !requestURI.equals("/api/auth/logout")
            || requestURI.equals("/api/boards")) {
            return true;
        }

        // Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7); // "Bearer " 제거

        // 토큰 검증
        if (!jwtProvider.validateToken(token)) {
            log.error("Invalid token.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 2. Redis JWT 확인
        if (!redisService.isJwt(jwtProvider.getJti(token))) {
            log.error("Token is logged out.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String email = jwtProvider.getEmail(token);
        String userName = jwtProvider.getDisplayName(token);
        // 3. Redis에 등록된 블랙리스트 검증
        if (redisService.isBlacklist(email, userName)) {
            log.error("email : '{}', userName : '{}' is blacklisted.", email, userName);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        request.setAttribute("userEmail", email);
        request.setAttribute("displayName", userName);

        return true;
    }
}
