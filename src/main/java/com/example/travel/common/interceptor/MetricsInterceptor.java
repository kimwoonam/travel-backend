package com.example.travel.common.interceptor;

import com.example.travel.common.metrics.MetricsConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.micrometer.core.instrument.Timer;

/**
 * API 요청 메트릭을 수집하는 인터셉터입니다.
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {
    
    private final MetricsConfig metricsConfig;
    private static final ThreadLocal<Timer.Sample> timerSample = new ThreadLocal<>();

    @Autowired
    public MetricsInterceptor(MetricsConfig metricsConfig) {
        this.metricsConfig = metricsConfig;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                            @NonNull HttpServletResponse response, 
                            @NonNull Object handler) {
        // 타이머 시작
        timerSample.set(metricsConfig.startTimer());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                               @NonNull HttpServletResponse response,
                               @NonNull Object handler,
                               @Nullable Exception ex) {
        try {
            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            int statusCode = response.getStatus();

            // API 요청 카운터 증가
            metricsConfig.incrementApiRequest(endpoint, method, statusCode);

            // 응답 시간 기록
            Timer.Sample sample = timerSample.get();
            if (sample != null) {
                metricsConfig.recordTimer(sample, endpoint, method);
            }

            // 에러 카운터 증가
            if (statusCode >= 400) {
                String errorType = statusCode >= 500 ? "server_error" : "client_error";
                metricsConfig.incrementError(endpoint, errorType);
            }

            if (ex != null) {
                metricsConfig.incrementError(endpoint, ex.getClass().getSimpleName());
            }
        } finally {
            timerSample.remove();
        }
    }
}
