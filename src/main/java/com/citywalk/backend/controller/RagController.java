package com.citywalk.backend.controller;

import com.citywalk.backend.service.PoiVectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final PoiVectorService poiVectorService;

    @GetMapping("/test-embedding")
    public String testEmbedding(@RequestParam String text) {
        float[] vector = embeddingModel.embed(text);
        return "向量维度：" + vector.length + "，前 5 个值：" +
                Arrays.toString(Arrays.copyOf(vector, 5));
    }

    @GetMapping("/init-poi-vectors")
    public String initPoiVectors() {
        int count = poiVectorService.initPoiVectors();
        return "成功向量化 " + count + " 个 POI";
    }

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
}