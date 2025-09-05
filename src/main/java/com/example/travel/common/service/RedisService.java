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

    public void addJwtBlacklist(String jti, long expiration) {
        // 블랙리스트에 JWT의 JTI를 추가하고 만료 시간을 설정
        redisTemplate.opsForValue().set("blacklist:" + jti, "invalid", expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isJwtBlacklisted(String jti) {
        // Redis에 JTI가 있는지 확인
        return redisTemplate.hasKey("blacklist:" + jti);
    }
}