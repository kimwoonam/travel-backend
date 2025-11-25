package com.moodo.travel.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 커스텀 헬스체크 인디케이터입니다.
 * 데이터베이스, Redis 등의 상태를 확인합니다.
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CustomHealthIndicator(JdbcTemplate jdbcTemplate, 
                                RedisTemplate<String, Object> redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        // 데이터베이스 상태 확인
        try {
            jdbcTemplate.execute("SELECT 1");
            details.put("database", "UP");
        } catch (Exception e) {
            details.put("database", "DOWN");
            details.put("database_error", e.getMessage());
            isHealthy = false;
        }

        // Redis 상태 확인
        try {
            redisTemplate.opsForValue().get("health_check");
            redisTemplate.opsForValue().set("health_check", "ok");
            details.put("redis", "UP");
        } catch (Exception e) {
            details.put("redis", "DOWN");
            details.put("redis_error", e.getMessage());
            isHealthy = false;
        }

        // JVM 메모리 정보
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        Map<String, Object> memoryDetails = new HashMap<>();
        memoryDetails.put("total", formatBytes(totalMemory));
        memoryDetails.put("used", formatBytes(usedMemory));
        memoryDetails.put("free", formatBytes(freeMemory));
        memoryDetails.put("max", formatBytes(maxMemory));
        memoryDetails.put("usage_percent", String.format("%.2f%%", memoryUsagePercent));
        details.put("memory", memoryDetails);

        // 메모리 사용률이 90% 이상이면 경고
        if (memoryUsagePercent > 90) {
            details.put("memory_warning", "Memory usage is above 90%");
            isHealthy = false;
        }

        // 스레드 정보
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        int threadCount = rootGroup.activeCount();
        details.put("threads", threadCount);

        if (isHealthy) {
            return Health.up()
                .withDetails(details)
                .build();
        } else {
            return Health.down()
                .withDetails(details)
                .build();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
