package com.moodo.travel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig는 RedisTemplate 빈을 설정하는 Spring 구성 클래스입니다.
 * 이 구성은 RedisTemplate에서 키, 값 및 해시 객체를 처리하는 데 사용되는 직렬화 메커니즘을 사용자 정의하는 역할을 합니다.
 * <br/>
 * 주요 기능:
 * - 키, 값, 해시 키 및 해시 값에 대한 직렬화를 구성합니다.
 * - 키와 해시 키에 StringRedisSerializer를 사용하여 사람이 읽을 수 있는 문자열로 저장합니다.
 * - 값과 해시 값에 GenericJackson2JsonRedisSerializer를 사용하여 복잡한 객체를 JSON 형식으로 저장합니다.
 * - 속성이 설정된 후 RedisTemplate 빈이 올바르게 구성되도록 합니다.
 * <br/>
 * 주요 구성 요소:
 * - `@Configuration`: 이 클래스가 Spring의 구성 클래스임을 나타냅니다.
 * - `@Bean`: Spring 컨테이너에서 관리할 RedisTemplate 빈을 선언합니다.
 * - RedisTemplate: Redis 작업을 실행하기 위한 고수준 추상화를 제공합니다.
 * - RedisConnectionFactory: Redis 연결을 설정하기 위한 주입된 연결 팩토리입니다.
 */
@Configuration
public class RedisConfig {

    /**
     * Redis 데이터 구조와 상호 작용하기 위한 {@link RedisTemplate} 빈을 생성하고 구성합니다.
     * 이 메서드는 키, 값, 해시 키 및 해시 값에 대한 직렬화기를 설정하여 Redis 작업 시 올바른
     * 직렬화 및 역직렬화 동작을 보장합니다. 이 메서드는 키 및 해시 키 직렬화에는
     * {@link StringRedisSerializer}를 사용하고, 값 및 해시 값 직렬화에는
     * {@link GenericJackson2JsonRedisSerializer}를 사용합니다.
     *
     * @param connectionFactory Redis 데이터베이스와의 연결을 설정하는 데 사용되는 {@link RedisConnectionFactory}입니다.
     *                          일반적으로 Spring Boot에서 자동으로 구성되거나 애플리케이션 구성에 명시적으로 정의됩니다.
     *
     * @return 구성된 직렬화 설정으로 Redis 작업을 수행하기 위한 완전히 구성된 {@link RedisTemplate} 인스턴스를 반환합니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 키(Key) 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        // 값(Value) 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash 키와 값에도 StringSerializer, Json 직렬화를 적용할 수 있습니다.
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();

        return template;
    }
}