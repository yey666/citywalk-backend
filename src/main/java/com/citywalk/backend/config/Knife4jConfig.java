package com.citywalk.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI citywalkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Citywalk API 文档")
                        .description("Citywalk AI 旅行规划平台后端接口")
                        .version("1.0.0"));
    }
}