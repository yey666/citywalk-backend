package com.citywalk.backend.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

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

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(model)
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        JedisPooled jedisPooled = new JedisPooled(
                new HostAndPort("localhost", 6379),
                DefaultJedisClientConfig.builder()
                        .database(0)
                        .build()
        );

        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("poi-index")
                .prefix("poi:")
                .initializeSchema(true)
                .build();
    }
}