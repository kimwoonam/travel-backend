package com.example.travel.common.service;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setJwt(String jti, long expiration) {
        redisTemplate.opsForValue().set("JWT:" + jti, true, expiration, TimeUnit.MILLISECONDS);
    }

    public void removeJwt(String jti) {
        redisTemplate.delete("JWT:" + jti);
    }

    public boolean isJwt(String jti) {
        return redisTemplate.hasKey("JWT:" + jti);
    }

    // 사용자 블랙리스트 체크
    public void addBlacklist(String email, String userName, long expiration) {
        // 블랙리스트에 JWT의 JTI를 추가하고 만료 시간을 설정
        redisTemplate.opsForValue()
            .set("blacklist:" + email + ":" + userName, "invalid", expiration,
                TimeUnit.MILLISECONDS);
    }

    // 사용자 블랙리스트 체크
    public boolean isBlacklist(String email, String userName) {
        // Redis에 JTI가 있는지 확인
        return redisTemplate.hasKey("blacklist:" + email + ":" + userName);
    }
}