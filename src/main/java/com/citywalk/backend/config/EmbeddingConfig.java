package com.citywalk.backend.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Value("${citywalk.embedding.api-key}")
    private String apiKey;

    @Value("${citywalk.embedding.base-url}")
    private String baseUrl;

    @Value("${citywalk.embedding.model}")
    private String model;

    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        // 用 model(...) 而不是 withModel(...)
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(model)
                .build();

        // 用三参数构造函数：(OpenAiApi, MetadataMode, OpenAiEmbeddingOptions)
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }
}