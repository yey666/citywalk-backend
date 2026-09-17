package com.citywalk.backend.controller;

import com.citywalk.backend.dto.RouteGenerateRequest;
import com.citywalk.backend.dto.RouteGenerateResponse;
import com.citywalk.backend.service.PoiVectorService;
import com.citywalk.backend.service.RouteGenerateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "AI 规划", description = "RAG 检索、路线生成、向量化")
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final PoiVectorService poiVectorService;
    private final RouteGenerateService routeGenerateService;

    @Operation(summary = "测试 Embedding")
    @GetMapping("/test-embedding")
    public String testEmbedding(@RequestParam String text) {
        float[] vector = embeddingModel.embed(text);
        return "向量维度：" + vector.length + "，前 5 个值：" +
                Arrays.toString(Arrays.copyOf(vector, 5));
    }

    @Operation(summary = "把所有 POI 向量化存入 Redis Stack")
    @GetMapping("/init-poi-vectors")
    public String initPoiVectors() {
        int count = poiVectorService.initPoiVectors();
        return "成功向量化 " + count + " 个 POI";
    }

    @Operation(summary = "向量检索测试")
    @GetMapping("/search")
    public List<Document> search(@RequestParam String query,
                                 @RequestParam(defaultValue = "5") int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
    }

    @Operation(summary = "AI 生成路线")
    @PostMapping("/generate-route")
    public RouteGenerateResponse generateRoute(@RequestBody RouteGenerateRequest request) {
        return routeGenerateService.generate(request);
    }
}