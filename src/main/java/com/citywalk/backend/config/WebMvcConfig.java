package com.citywalk.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")     // 拦截所有 API
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/city/**",
                        "/api/rag/**",
                        "/api/route/generate",
                        "/api/route/draft/optimize"     // ← 加这个
                );
    }
}