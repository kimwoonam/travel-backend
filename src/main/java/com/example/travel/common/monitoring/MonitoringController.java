package com.example.travel.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 애플리케이션 모니터링 정보를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MeterRegistry meterRegistry;

    @Autowired
    public MonitoringController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 애플리케이션 상태 정보를 반환합니다.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 기본 정보
        status.put("timestamp", LocalDateTime.now());
        status.put("status", "UP");
        
        // JVM 정보
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("totalMemory", runtime.totalMemory());
        jvmInfo.put("freeMemory", runtime.freeMemory());
        jvmInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvmInfo.put("maxMemory", runtime.maxMemory());
        jvmInfo.put("availableProcessors", runtime.availableProcessors());
        status.put("jvm", jvmInfo);
        
        // 스레드 정보
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        status.put("threadCount", rootGroup.activeCount());
        
        return ResponseEntity.ok(status);
    }

    /**
     * 메트릭 정보를 반환합니다.
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // API 요청 카운터
        Counter apiRequests = meterRegistry.find("api.requests.total").counter();
        if (apiRequests != null) {
            metrics.put("api_requests_total", apiRequests.count());
        }
        
        // API 에러 카운터
        Counter apiErrors = meterRegistry.find("api.errors.total").counter();
        if (apiErrors != null) {
            metrics.put("api_errors_total", apiErrors.count());
        }
        
        // API 응답 시간
        Timer apiDuration = meterRegistry.find("api.request.duration").timer();
        if (apiDuration != null) {
            Map<String, Object> durationMetrics = new HashMap<>();
            durationMetrics.put("count", apiDuration.count());
            durationMetrics.put("mean_ms", apiDuration.mean(TimeUnit.MILLISECONDS));
            durationMetrics.put("max_ms", apiDuration.max(TimeUnit.MILLISECONDS));
            // percentile은 DistributionSummary에서만 사용 가능
            metrics.put("api_request_duration", durationMetrics);
        }
        
        // JVM 메모리 메트릭
        Gauge memoryUsageGauge = meterRegistry.find("jvm.memory.usage.percent").gauge();
        if (memoryUsageGauge != null) {
            double memoryUsage = memoryUsageGauge.value();
            if (!Double.isNaN(memoryUsage)) {
                metrics.put("jvm_memory_usage_percent", memoryUsage);
            }
        }
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * 시스템 정보를 반환합니다.
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // OS 정보
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", System.getProperty("os.name"));
        osInfo.put("version", System.getProperty("os.version"));
        osInfo.put("arch", System.getProperty("os.arch"));
        systemInfo.put("os", osInfo);
        
        // Java 정보
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        javaInfo.put("runtime", System.getProperty("java.runtime.name"));
        systemInfo.put("java", javaInfo);
        
        // 애플리케이션 정보
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", "travel-backend");
        appInfo.put("timestamp", LocalDateTime.now());
        systemInfo.put("application", appInfo);
        
        return ResponseEntity.ok(systemInfo);
    }
}
