package com.example.travel.common.service;

import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger log = LogManager.getLogger(RedisService.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setJwt(String jti) {
        redisTemplate.opsForValue().set("JWT:" + jti, true, 3600000, TimeUnit.MILLISECONDS);
    }

    public void removeJwt(String jti) {
        redisTemplate.delete("JWT:" + jti);
    }

    public boolean isJwt(String jti) {
        return redisTemplate.hasKey("JWT:" + jti);
    }

    // 사용자 블랙리스트 체크
    public void addBlacklist(String email, String userName) {
        // 블랙리스트에 JWT의 JTI를 추가하고 만료 시간을 설정
        redisTemplate.opsForValue()
            .set("blacklist:" + email + ":" + userName, "invalid", 3600000,
                TimeUnit.MILLISECONDS);
    }

    // 사용자 블랙리스트 체크
    public boolean isBlacklist(String email, String userName) {
        // Redis에 JTI가 있는지 확인
        return redisTemplate.hasKey("blacklist:" + email + ":" + userName);
    }
}