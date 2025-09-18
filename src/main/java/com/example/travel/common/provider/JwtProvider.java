package com.example.travel.common.provider;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 이 클래스는 JSON 웹 토큰(JWT)을 생성, 파싱 및 검증하는 유틸리티 메서드를 제공합니다.
 * 사용자 정보와 만료 정보가 포함된 토큰을 안전하게 생성하고, 토큰을 검증하고,
 * 토큰에서 특정 클레임(예: 이메일, 표시 이름, 만료)을 추출하는 기능을 제공합니다.
 * <br/>
 * 이 클래스는 구성 가능한 비밀 키와 만료 시간을 사용하여 토큰에 서명하고 유효성을 검사합니다. 토큰 작업을 위해
 * JJWT 라이브러리를 사용합니다.
 * <br/>
 * 주요 기능은 다음과 같습니다.
 * - 사용자 세부 정보 및 만료 정보를 사용하여 JWT 토큰을 생성합니다.
 * - 제공된 JWT 토큰의 진위 및 무결성을 검증합니다.
 * - 제공된 토큰에서 이메일, 표시 이름, 만료일과 같은 특정 클레임을 추출합니다.
 * <br/>
 * 구성:
 * - `jwt.secret`: JWT 서명에 사용되는 비밀 키입니다. 기본값: "created-cursor-ai-travel-backend-secret-key"입니다.
 * - `jwt.expiration`: 토큰이 유효한 기간(밀리초)입니다. 기본값: 3600000(1시간)입니다.
 */
@Component
public class JwtProvider {

    @Value("${jwt.secret:created-cursor-ai-travel-backend-secret-key}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 24시간
    private long expiration;

    /**
     * JWT 서명에 사용되는 {@link SecretKey}를 생성하고 반환합니다. 키는 구성된 비밀 값에서 파생됩니다.
     *
     * @return JWT 서명을 위한 비밀 키를 반환
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 제공된 이메일 주소와 표시 이름을 기반으로 JWT 토큰을 생성합니다. 토큰에는 사용자 정보와 만료 시간이 포함됩니다.
     *
     * @param email 토큰이 생성된 사용자의 이메일
     * @param name  토큰 클레임에 포함할 사용자의 표시 이름
     * @return 생성된 JWT 토큰을 문자열로 반환
     */
    public String generateToken(String email, String name) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(email)
            .id(UUID.randomUUID().toString())
            .claim("name", name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 주어진 JWT 토큰의 진위성과 무결성을 검증합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return {@code true} 토큰이 유효하고 적절하게 서명된 경우, {@code false} 그렇지 않은 경우
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 제공된 JWT 토큰에서 이메일 주소를 추출하고 검색합니다. 이메일은 파싱된 토큰 페이로드의 제목 클레임에서 가져옵니다.
     *
     * @param token 이메일 주소를 추출할 JWT 토큰
     * @return 검색된 이메일 주소
     */
    public String getEmail(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * 제공된 JWT 토큰에서 표시 이름을 추출하고 검색합니다. 표시 이름은 토큰 페이로드의 "name" 클레임에서 검색됩니다.
     *
     * @param token 표시 이름을 추출할 JWT 토큰
     * @return 토큰에서 검색된 표시 이름을 반환
     */
    public String getName(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("name", String.class);
    }

    /**
     * 제공된 JWT 토큰에서 만료일을 추출하고 검색합니다. 만료일은 토큰이 더 이상 유효하지 않게 되는 시점을 나타냅니다.
     *
     * @param token 만료 날짜를 추출할 JWT 토큰
     * @return 토큰에서 검색된 만료 날짜를 반환하거나 토큰이 유효하지 않거나 만료 날짜를 포함하지 않는 경우 {@code null}을 반환
     */
    public Date getExpirationDate(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration();
    }

}