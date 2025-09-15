package com.example.travel.common.provider;

import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisProvider {

    private static final Logger log = LogManager.getLogger(RedisProvider.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setJwt(String token) {
        redisTemplate.opsForValue().set("JWT:" + token, true, 3600000, TimeUnit.MILLISECONDS);
    }

    public void removeJwt(String token) {
        redisTemplate.delete("JWT:" + token);
    }

    public boolean isJwt(String token) {
        return redisTemplate.hasKey("JWT:" + token);
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