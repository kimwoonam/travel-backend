package com.moodo.travel.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 캐시 설정 클래스입니다.
 * Redis를 캐시 저장소로 사용합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis 캐시 매니저를 생성합니다.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // 기본 TTL: 10분
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues(); // null 값 캐싱 비활성화

        // 캐시별 설정
        RedisCacheConfiguration boardCacheConfig = config.entryTtl(Duration.ofMinutes(30)); // 게시판 캐시: 30분
        RedisCacheConfiguration boardDetailCacheConfig = config.entryTtl(Duration.ofHours(1)); // 게시판 상세: 1시간

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("boards", boardCacheConfig)
            .withCacheConfiguration("board", boardDetailCacheConfig)
            .transactionAware()
            .build();
    }
}




