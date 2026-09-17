package com.citywalk.backend.service.impl;

import com.citywalk.backend.dto.RouteGenerateRequest;
import com.citywalk.backend.dto.RouteGenerateResponse;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.service.RouteGenerateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteGenerateServiceImpl implements RouteGenerateService {

    private final VectorStore vectorStore;
    private final PoiMapper poiMapper;
    private final ChatModel chatModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RouteGenerateResponse generate(RouteGenerateRequest request) {
        // 1. 拼接检索文本
        String query = request.getTheme() + " " +
                request.getDifficulty() + " " +
                request.getDuration() + "小时";

        log.info("检索文本：{}", query);

        // 2. 向量检索 Top 10 POI
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(10)
                        .build()
        );

        if (docs.isEmpty()) {
            throw new RuntimeException("未找到相关 POI");
        }

        // 3. 诊断日志：打印每个文档的完整 metadata
        log.info("===== 检索到 {} 个文档 =====", docs.size());
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            log.info("文档 [{}] metadata: {}", i, doc.getMetadata());
            log.info("文档 [{}] metadata 所有 key: {}", i, doc.getMetadata().keySet());
        }

        // 4. 提取 POI ID（防御性写法）
        // 4. 从 text 中用正则提取 poiId
        List<Long> poiIds = docs.stream()
                .map(d -> {
                    String text = d.getText();
                    log.info("文档 text: {}", text);
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("poiId:(\\d+)")
                            .matcher(text);
                    if (m.find()) {
                        return Long.parseLong(m.group(1));
                    }
                    log.warn("文档 text 里没有 poiId：{}", text);
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (poiIds.isEmpty()) {
            throw new RuntimeException("检索到的文档缺少 poiId 元数据，无法反查 POI");
        }

        log.info("提取到 {} 个有效 poiId：{}", poiIds.size(), poiIds);

        // 5. 从 MySQL 查完整 POI 信息
        List<Poi> pois = poiMapper.selectBatchIds(poiIds);
        log.info("从数据库查到 {} 个 POI", pois.size());

        if (pois.isEmpty()) {
            throw new RuntimeException("数据库中找不到对应的 POI");
        }

        // 6. 构建 Prompt
        String prompt = buildPrompt(request, pois);

        // 7. 调 DeepSeek 生成路线
        String aiResponse = ChatClient.builder(chatModel)
                .build()
                .prompt(prompt)
                .call()
                .content();

        log.info("DeepSeek 返回：{}", aiResponse);

        // 8. 解析 JSON 响应
        RouteGenerateResponse response = parseAiResponse(aiResponse, pois);
        response.setTheme(request.getTheme());
        response.setDuration(request.getDuration());
        response.setDifficulty(request.getDifficulty());

        return response;
    }

    private String buildPrompt(RouteGenerateRequest request, List<Poi> pois) {
        StringBuilder poiList = new StringBuilder();
        for (Poi poi : pois) {
            poiList.append(String.format("- id=%d, 名称=%s, 类别=%s, 地址=%s\n",
                    poi.getId(), poi.getName(), poi.getCategory(), poi.getAddress()));
        }

        return String.format("""
                你是苏州 Citywalk 路线规划助手。
                
                用户需求：
                - 时长：%d 小时
                - 主题：%s
                - 体力：%s
                
                可选的 POI 列表：
                %s
                
                请从上面的 POI 中选出 3-5 个，编排成一条路线。
                
                约束：
                1. 只能从给定的 POI 列表中选择，不要编造新的 POI
                2. 每个 POI 的停留时间 30-120 分钟
                3. 总时长控制在 %d 小时左右
                4. 返回严格的 JSON 格式，不要 markdown 代码块：
                
                {
                  "title": "路线标题",
                  "nodes": [
                    {
                      "order": 1,
                      "poiId": 1,
                      "poiName": "拙政园",
                      "stayDuration": 90,
                      "tip": "本地人提示",
                      "photoSpot": "机位描述"
                    }
                  ]
                }
                """,
                request.getDuration(),
                request.getTheme(),
                request.getDifficulty(),
                poiList.toString(),
                request.getDuration()
        );
    }

    private RouteGenerateResponse parseAiResponse(String aiResponse, List<Poi> pois) {
        try {
            // 去掉可能的 markdown 代码块
            String json = aiResponse.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            JsonNode root = objectMapper.readTree(json);
            RouteGenerateResponse response = new RouteGenerateResponse();
            response.setTitle(root.get("title").asText());

            List<RouteGenerateResponse.RouteNode> nodes = new ArrayList<>();
            for (JsonNode node : root.get("nodes")) {
                RouteGenerateResponse.RouteNode rn = new RouteGenerateResponse.RouteNode();
                rn.setOrder(node.get("order").asInt());
                rn.setPoiId(node.get("poiId").asLong());
                rn.setPoiName(node.get("poiName").asText());
                rn.setStayDuration(node.get("stayDuration").asInt());
                rn.setTip(node.has("tip") ? node.get("tip").asText() : "");
                rn.setPhotoSpot(node.has("photoSpot") ? node.get("photoSpot").asText() : "");

                // 从 pois 里补 lat/lng
                pois.stream()
                        .filter(p -> p.getId().equals(rn.getPoiId()))
                        .findFirst()
                        .ifPresent(p -> {
                            rn.setLat(p.getLat().doubleValue());
                            rn.setLng(p.getLng().doubleValue());
                        });

                nodes.add(rn);
            }
            response.setNodes(nodes);
            return response;
        } catch (Exception e) {
            log.error("解析 AI 响应失败", e);
            throw new RuntimeException("AI 返回格式错误：" + e.getMessage());
        }
    }
}