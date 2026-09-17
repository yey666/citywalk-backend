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
                // 拦截的路径
                .addPathPatterns(
                        "/api/user/**",
                        "/api/route/*/save",
                        "/api/route/*/collect",
                        "/api/post/**"
                )
                // 不拦截的路径
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/city/**",
                        "/api/rag/**",
                        "/api/route/generate",
                        "/api/route/*"  // 看路线详情不需要登录
                );
    }
}