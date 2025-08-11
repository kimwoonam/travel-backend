package com.example.travel.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 CORS preflight 요청이므로 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 인증이 필요하지 않은 엔드포인트들
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") && !requestURI.equals("/api/auth/logout") || requestURI.equals("/api/boards")) {
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
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 토큰에서 이메일과 사용자 이름 추출하여 요청 속성에 저장
        JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
        request.setAttribute("userEmail", tokenInfo.getEmail());
        request.setAttribute("displayName", tokenInfo.getDisplayName());

        return true;
    }
}
