package com.example.travel.common.metrics;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 성능 메트릭 수집을 위한 설정 클래스입니다.
 */
@Configuration
@EnableScheduling
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * @Timed 어노테이션을 사용하기 위한 Aspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * JVM 메모리 메트릭 수집
     */
    @Scheduled(fixedRate = 60000) // 1분마다
    public void collectJvmMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        meterRegistry.gauge("jvm.memory.total", totalMemory);
        meterRegistry.gauge("jvm.memory.free", freeMemory);
        meterRegistry.gauge("jvm.memory.used", usedMemory);
        meterRegistry.gauge("jvm.memory.max", maxMemory);
        meterRegistry.gauge("jvm.memory.usage.percent", 
            (double) usedMemory / maxMemory * 100);
    }

    /**
     * API 요청 카운터 생성
     */
    public void incrementApiRequest(String endpoint, String method, int statusCode) {
        meterRegistry.counter("api.requests.total",
            "endpoint", endpoint,
            "method", method,
            "status", String.valueOf(statusCode)
        ).increment();
    }

    /**
     * API 응답 시간 측정
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordTimer(Timer.Sample sample, String endpoint, String method) {
        sample.stop(Timer.builder("api.request.duration")
            .description("API 요청 처리 시간")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .register(meterRegistry));
    }

    /**
     * 에러 카운터 증가
     */
    public void incrementError(String endpoint, String errorType) {
        meterRegistry.counter("api.errors.total",
            "endpoint", endpoint,
            "error_type", errorType
        ).increment();
    }
}

