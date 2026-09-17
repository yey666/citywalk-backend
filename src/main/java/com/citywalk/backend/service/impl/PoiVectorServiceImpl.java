package com.citywalk.backend.service.impl;

import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.service.PoiVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoiVectorServiceImpl implements PoiVectorService {

    private final PoiMapper poiMapper;
    private final VectorStore vectorStore;

    @Override
    public int initPoiVectors() {
        // 1. 从 MySQL 读所有 POI
        List<Poi> pois = poiMapper.selectList(null);
        log.info("从数据库读到 {} 个 POI", pois.size());

        // 2. 构建 Document 列表
        List<Document> documents = new ArrayList<>();
        for (Poi poi : pois) {
            // 拼接检索文本：名称 + 类别 + 地址
            String text = "poiId:" + poi.getId() + " " +
                    (poi.getName() != null ? poi.getName() : "") + " " +
                    (poi.getCategory() != null ? poi.getCategory() : "") + " " +
                    (poi.getAddress() != null ? poi.getAddress() : "");

            // 元数据：保留 poiId，检索后能用它反查 POI
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("poiId", poi.getId());
            metadata.put("name", poi.getName());
            metadata.put("cityId", poi.getCityId());

            Document doc = new Document(text, metadata);
            documents.add(doc);
        }

        // 3. 批量存入 VectorStore（内部会调 Embedding 模型向量化）
        log.info("开始向量化并存入 Redis Stack...");
        vectorStore.add(documents);
        log.info("向量化完成，共存入 {} 条", documents.size());

        return documents.size();
    }
}