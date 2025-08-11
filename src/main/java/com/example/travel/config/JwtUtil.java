package com.example.travel.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:created-cursor-ai-travel-backend-secret-key}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24시간
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String displayName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("displayName", displayName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getDisplayNameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("displayName", String.class);
    }

    public static class TokenInfo {
        private final String email;
        private final String displayName;

        public TokenInfo(String email, String displayName) {
            this.email = email;
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public TokenInfo getTokenInfo(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String email = claims.getSubject();
            String displayName = claims.get("displayName", String.class);
            
            return new TokenInfo(email, displayName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

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
}
