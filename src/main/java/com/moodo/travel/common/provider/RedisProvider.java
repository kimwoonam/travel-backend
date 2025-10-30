package com.moodo.travel.common.provider;

import com.moodo.travel.account.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 및 사용자 블랙리스트 정보를 관리하는 Redis 기반 공급자입니다.
 * 이 클래스는 Redis를 활용하여 토큰 저장, 유효성 검사, 제거와 같은 작업을 수행하고
 * <br/>
 * 주요 기능은 다음과 같습니다.
 * - Redis 캐시에서 만료 시간을 사용하여 JWT 토큰을 관리합니다.
 * - 특정 사용자 식별자에 연결된 만료 시간을 사용하여 사용자 블랙리스트를 관리합니다.
 * <br/>
 * Redis 캐시와의 상호작용을 위해 Spring RedisTemplate을 사용합니다.
 */
@Component
public class RedisProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 만료 시간이 1시간인 JWT 토큰을 Redis 캐시에 저장합니다.
     *
     * @param token Redis 캐시에 저장될 JWT 토큰
     */
    public void setJwt(String token) {
        redisTemplate.opsForValue().set("JWT:" + token, true, 3600000, TimeUnit.MILLISECONDS);
    }

    /**
     * Redis 캐시에서 지정된 JWT 토큰을 제거합니다.
     *
     * @param token Redis 캐시에서 제거할 JWT 토큰
     */
    public void removeJwt(String token) {
        redisTemplate.delete("JWT:" + token);
    }

    /**
     * 지정된 JWT 토큰이 Redis 캐시에 있는지 확인합니다.
     *
     * @param token Redis 캐시에서 확인할 JWT 토큰
     * @return Redis 캐시에 JWT 토큰이 있으면 true를 반환하고, 그렇지 않으면 false를 반환
     */
    public boolean isJwt(String token) {
        return redisTemplate.hasKey("JWT:" + token);
    }

    /**
     * 사용자 정보를 Redis 캐시에 저장합니다. 이 정보에는 사용자의 이메일, 이름, UUID가 포함됩니다.
     * 데이터는 1시간 후 만료되도록 설정됩니다.
     *
     * @param account Redis에 저장될 사용자 정보를 포함하는 계정 객체입니다. 이메일, 이름, UUID를 속성으로 포함합니다.
     */
    public void setUserInfo(Account account) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", account.getEmail());
        userInfo.put("name", account.getName());
        userInfo.put("uuid", account.getUuid());

        redisTemplate.opsForHash().putAll("USERINFO:" + account.getEmail(), userInfo);
        redisTemplate.expire("USERINFO:" + account.getEmail(), 3600000, TimeUnit.MILLISECONDS);
    }

    /**
     * Redis 캐시에서 사용자 정보를 검색하여 Account 객체에 매핑합니다.
     *
     * @param email 정보를 검색할 사용자의 이메일 주소
     * @return 캐시에서 검색된 사용자 정보가 포함된 Account 객체를 반환
     */
    public Optional<Account> getUserInfo(String email) {

        // redisTemplate.opsForHash().get("USERINFO:" + email, "EMAIL")
        Map<Object, Object> userInfo = redisTemplate.opsForHash().entries("USERINFO:" + email);
        Account account = new ObjectMapper().convertValue(userInfo, Account.class);
        return Optional.ofNullable(account);
    }

    /**
     * 지정된 이메일 주소와 연관된 사용자 정보를 Redis 캐시에서 제거합니다.
     *
     * @param email Redis 캐시에서 정보를 제거해야 하는 사용자의 이메일 주소
     */
    public void removeUserInfo(String email) {
        redisTemplate.delete("USERINFO:" + email);
    }

    /**
     * Redis 캐시의 블랙리스트에 특정 사용자 정보를 만료 시간과 함께 추가합니다.
     *
     * @param email 블랙리스트에 등록할 사용자의 이메일 주소
     * @param name  블랙리스트에 등록할 사용자의 사용자 이름
     */
    public void addBlacklist(String email, String name) {
        redisTemplate.opsForValue()
            .set("blacklist:" + email + ":" + name, "invalid", 3600000,
                TimeUnit.MILLISECONDS);
    }

    /**
     * 지정된 사용자 정보가 Redis 캐시의 블랙리스트에 있는지 확인합니다.
     *
     * @param email 블랙리스트에 등록한 사용자의 이메일 주소
     * @param name  블랙리스트에 등록한 사용자의 사용자 이름
     * @return 사용자 정보가 블랙리스트에 있으면 true, 그렇지 않으면 false
     */
    public boolean isBlacklist(String email, String name) {
        // Redis에 JTI가 있는지 확인
        return redisTemplate.hasKey("blacklist:" + email + ":" + name);
    }
}