package com.example.travel.common.interceptor;

import com.example.travel.common.provider.JwtProvider;
import com.example.travel.common.provider.RedisProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JwtInterceptor는 {@link HandlerInterceptor}의 구현체로,
 * 수신 HTTP 요청에 대한 JWT 기반 인증 및 권한 부여를 처리하도록 설계되었습니다.
 * 제공된 JWT를 검증하고, JwtProvider와 RedisProvider를 통해 유효성을 검사하고,
 * 블랙리스트 및 토큰 만료와 같은 보안 정책을 적용하여 요청이 컨트롤러에 도달하기 전에 요청을 처리합니다.
 * <br/>
 * 흐름은 다음과 같습니다.
 * 1. 사전 실행 OPTIONS 요청을 즉시 허용합니다.
 * 2. 미리 정의된 특정 엔드포인트에 대한 인증을 건너뜁니다.
 * 3. Bearer 토큰의 포함 및 형식을 위해 'Authorization' 헤더를 검증합니다.
 * 4. JwtProvider를 사용하여 JWT를 디코딩하고 검증합니다.
 * 5. RedisProvider를 통해 Redis에서 토큰의 존재 여부와 블랙리스트 상태를 확인합니다.
 * 6. 토큰에서 이메일 및 표시 이름과 같은 사용자 정보를 추출하고 요청 속성으로 설정하여 후속 핸들러에서 사용할 수 있도록 합니다.
 * <br/>
 * 인증이나 유효성 검사가 실패하면 HTTP 401 Unauthorized 상태로 응답합니다.
 * 이 클래스는 Spring 컴포넌트로 구성되어 요청 처리 중에 핸들러 체인에 삽입될 수 있습니다.
 * <br/>
 * 종속성:
 * - {@link JwtProvider}: JWT 생성, 유효성 검사 및 토큰 콘텐츠 추출을 처리합니다.
 * - {@link RedisProvider}: Redis 데이터 저장소에서 토큰 존재 여부 확인 및 블랙리스트 작성과 같은 토큰 관련 작업을 관리합니다.
 * <br/>
 * 보안:
 * 이 구현은 사전 CORS 또는 제한 없는 엔드포인트를 허용하는 동시에,
 * 승인된 요청에 유효하고 블랙리스트에 포함되지 않은 토큰이 존재하도록 하여 안전한 요청 처리를 보장합니다.
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LogManager.getLogger(JwtInterceptor.class);
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;

    @Autowired
    public JwtInterceptor(JwtProvider jwtProvider, RedisProvider redisProvider) {
        this.jwtProvider = jwtProvider;
        this.redisProvider = redisProvider;
    }

    /**
     * 컨트롤러에 도달하기 전에 HTTP 요청을 전처리합니다. 요청의 권한 헤더를 검증하고, JWT를 추출 및 검증하고,
     * Redis에 토큰이 있는지 확인하고, 사용자가 블랙리스트에 등록되어 있지 않은지 확인합니다.
     * 검증이 성공하면 사용자 이메일과 표시 이름을 요청 속성에 추가합니다.
     *
     * @param request 처리 중인 HTTP 요청
     * @param response 준비 중인 HTTP 응답
     * @param handler 유형 및/또는 인스턴스 검사를 위해 실행할 선택된 핸들러
     * @return {@code true} 요청이 핸들러로 진행되도록 허용된 경우,
     *         {@code false} 요청이 승인되지 않은 경우
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        // OPTIONS 요청은 CORS preflight 요청이므로 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 인증이 필요하지 않은 엔드포인트들
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") && !requestURI.equals("/api/auth/logout")) {
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
        if (!redisProvider.isJwt(token)) {
            log.error("Token is logged out.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String email = jwtProvider.getEmail(token);
        String name = jwtProvider.getName(token);
        // 3. Redis에 등록된 블랙리스트 검증
        if (redisProvider.isBlacklist(email, name)) {
            log.error("email : '{}', name : '{}' is blacklisted.", email, name);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        request.setAttribute("email", email);
        request.setAttribute("name", name);

        return true;
    }
}
