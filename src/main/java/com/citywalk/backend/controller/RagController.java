package com.citywalk.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final EmbeddingModel embeddingModel;

    @GetMapping("/test-embedding")
    public String testEmbedding(@RequestParam String text) {
        try {
            System.out.println("===== 开始调用 Embedding =====");
            System.out.println("文本：" + text);

            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
            float[] vector = response.getResults().get(0).getOutput();

            System.out.println("===== 调用成功，向量维度：" + vector.length);
            return "向量维度：" + vector.length + "，前 5 个值：" +
                    Arrays.toString(Arrays.copyOf(vector, 5));
        } catch (Exception e) {
            System.out.println("===== 调用失败 =====");
            e.printStackTrace();
            return "调用失败：" + e.getMessage() + " | 异常类型：" + e.getClass().getName();
        }
    }
}