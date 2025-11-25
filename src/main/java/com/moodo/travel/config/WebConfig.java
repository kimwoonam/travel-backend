package com.moodo.travel.config;

import com.moodo.travel.common.interceptor.JwtInterceptor;
import com.moodo.travel.common.interceptor.MetricsInterceptor;
import com.moodo.travel.common.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * WebConfig는 Spring의 {@link WebMvcConfigurer}를 구현하여 애플리케이션의 Web MVC 설정을 구성합니다.
 * 이 클래스는 주로 사용자 정의 인터셉터(JwtInterceptor)를 구성하고 등록하여 특정
 * HTTP 요청 경로에 대한 인증 및 권한 부여를 처리합니다.
 * <br/>
 * 해당 구성 클래스는 JwtInterceptor와 함께 동작하며, 애플리케이션 내
 * 특정 엔드포인트에 인증 논리를 추가 및 관리합니다.
 * <br/>
 * 주요 특징:
 * - 전역 인터셉터 설정을 통해 요청 단위의 보안 정책 적용.
 * - 특정 경로에 인터셉터를 등록하여 선택적 경로 보안 제공.
 * - 특정 경로는 인터셉터에서 제외하도록 구성 가능.
 * <br/>
 * 구성 요소:
 * - `JwtInterceptor`: JWT 기반 인증 및 권한 요구 사항을 처리하기 위한 커스텀 인터셉터.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final MetricsInterceptor metricsInterceptor;

    @Autowired
    public WebConfig(JwtInterceptor jwtInterceptor, RateLimitInterceptor rateLimitInterceptor, MetricsInterceptor metricsInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.metricsInterceptor = metricsInterceptor;
    }

    /**
     * 사용자 지정 JWT 인터셉터를 추가하여 애플리케이션의 인터셉터 레지스트리를 구성합니다.
     * 이 메서드는 인터셉터를 적용할 경로와 제외할 경로를 지정합니다.
     *
     * @param registry 커스텀 인터셉터를 등록하는 데 사용되는 {@link InterceptorRegistry}입니다.
     *                 이는 Spring 프레임워크에서 구성 설정 시 제공됩니다.
     *                 이 메서드는 이를 사용하여 `JwtInterceptor` 인스턴스를 추가하고
     *                 특정 요청 패턴에 적용합니다.
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Rate Limiting 인터셉터를 먼저 등록 (순서가 중요)
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**");

        // 메트릭 수집 인터셉터 등록
        registry.addInterceptor(metricsInterceptor)
            .addPathPatterns("/api/**");

        // JWT 인터셉터 등록
        registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**"
                , "/api/file/download/**"
            );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ** (1) 정적 자원 핸들러: build 폴더의 파일을 처리합니다.**
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true) // 성능 최적화를 위한 리소스 n체인 활성화
            .addResolver(new PathResourceResolver() {
                @Override
                protected org.springframework.core.io.Resource getResource(String resourcePath,
                    Resource location) throws java.io.IOException {
                    Resource requestedResource = location.createRelative(resourcePath);

                    // ** (2) 자원이 존재하면 해당 자원을 반환합니다.**
                    if (requestedResource.exists() && requestedResource.isReadable()) {
                        return requestedResource;
                    }

                    // ** (3) 자원이 없으면 index.html을 반환하여 React 라우팅에 넘깁니다.**
                    if (resourcePath.startsWith("api") || resourcePath.startsWith("ws")) {
                        // API 경로는 폴백 대상에서 제외 (선택 사항)
                        return null;
                    }
                    return location.createRelative("index.html");
                }
            });
    }
}