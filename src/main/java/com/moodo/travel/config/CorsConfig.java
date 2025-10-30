package com.moodo.travel.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CorsConfig는 애플리케이션의 CORS(Cross-Origin Resource Sharing) 설정을 지정하는 Spring 구성 클래스입니다.
 * <br/>
 * 이 구성을 통해 애플리케이션은 보안을 유지하면서 다양한 출처의 요청을 처리할 수 있습니다.
 * 허용되는 출처는 애플리케이션 구성 파일에 정의된 외부 속성을 통해 구성됩니다.
 * <br/>
 * 주요 기능:
 * - 지정된 출처의 요청을 허용합니다.
 * - CORS 요청에서 허용되는 헤더와 메서드를 구성합니다.
 * - CORS 요청에 자격 증명을 포함할지 여부를 설정합니다.
 * <br/>
 * 구성 요소:
 * - `@Configuration`: 이 클래스를 Spring의 구성 클래스로 표시합니다.
 * - `@Bean`: CorsFilter에 대한 Spring 관리 빈을 선언합니다.
 * - `CorsConfiguration`: 허용된 출처, 헤더, 메서드, 자격 증명과 같은 CORS 관련 설정을 구성합니다.
 * - `UrlBasedCorsConfigurationSource`: CORS 구성을 URL 패턴에 매핑합니다.
 * - `CorsFilter`: 구성된 설정에 따라 CORS 요청을 처리하는 필터를 구현합니다.
 * <br/>
 * 애플리케이션 속성:
 * - `app.cors.allowed-origins`: CORS에 허용되는 출처를 쉼표로 구분하여 나열한 목록입니다.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 애플리케이션의 CORS(Cross-Origin Resource Sharing) 요청을 처리하기 위한
     * {@link CorsFilter} 빈을 구성하고 제공합니다. 이 구성을 통해 애플리케이션 속성에 정의된 특정 출처,
     * 헤더 및 메서드를 사용할 수 있으며, 필요한 경우 자격 증명을 지원하면서 CORS 요청을 안전하게 처리할 수 있습니다.
     *
     * @return 허용된 출처, 헤더, 메서드, 자격 증명 지원을 포함한 CORS 설정으로 구성된 {@link CorsFilter} 인스턴스입니다.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // config.setAllowCredentials(false);
        // config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
