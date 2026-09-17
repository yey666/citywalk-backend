package com.citywalk.backend.service.impl;

import com.citywalk.backend.dto.TrainQueryResponse;
import com.citywalk.backend.service.TrainService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TrainServiceImpl implements TrainService {

    @Value("${citywalk.mcp.base-url}")
    private String mcpBaseUrl;

    @Value("${citywalk.mcp.endpoint}")
    private String mcpEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TrainQueryResponse query(String from, String to, String date) {
        try {
            // 1. initialize 拿 session id
            String sessionId = initialize();
            log.info("拿到 MCP session: {}", sessionId);

            // 2. 调 tools/call 查票价
            String rawResponse = callTool(sessionId, from, to, date);
            log.info("MCP 原始返回：{}", rawResponse);

            // 3. 解析并转换成前端格式
            return parseResponse(rawResponse, from, to, date);

        } catch (Exception e) {
            log.error("查询 12306 失败", e);
            throw new RuntimeException("票价查询失败：" + e.getMessage());
        }
    }

    /**
     * 第一步：initialize，拿 session id
     */
    private String initialize() {
        String url = mcpBaseUrl + mcpEndpoint;

        Map<String, Object> body = new HashMap<>();
        body.put("jsonrpc", "2.0");
        body.put("method", "initialize");
        body.put("id", 1);

        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", new HashMap<>());
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("name", "citywalk-backend");
        clientInfo.put("version", "1.0.0");
        params.put("clientInfo", clientInfo);
        body.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
        // 从响应头拿 mcp-session-id
        List<String> sessionIds = response.getHeaders().get("mcp-session-id");
        if (sessionIds == null || sessionIds.isEmpty()) {
            throw new RuntimeException("MCP 未返回 session id");
        }
        return sessionIds.get(0);
    }

    /**
     * 第二步：调 tools/call 查票价
     */
    private String callTool(String sessionId, String from, String to, String date) {
        String url = mcpBaseUrl + mcpEndpoint;

        Map<String, Object> body = new HashMap<>();
        body.put("jsonrpc", "2.0");
        body.put("method", "tools/call");
        body.put("id", 2);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "query-ticket-price");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("from_station", from);
        arguments.put("to_station", to);
        arguments.put("train_date", date);
        params.put("arguments", arguments);

        body.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM));
        headers.set("mcp-session-id", sessionId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
        return new String(response.getBody(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 第三步：解析 SSE 格式的响应
     */
    private TrainQueryResponse parseResponse(String rawResponse, String from, String to, String date) throws Exception {
        // SSE 格式：event: message\ndata: {...}\n\n
        // 提取 data: 后面的 JSON
        String json = null;
        for (String line : rawResponse.split("\n")) {
            if (line.startsWith("data: ")) {
                json = line.substring(6);
                break;
            }
        }
        if (json == null) {
            throw new RuntimeException("MCP 返回格式错误，没有 data 行");
        }

        // 解析 JSON-RPC 响应
        JsonNode root = objectMapper.readTree(json);

        // 提取 result.content[0].text
        JsonNode contentArray = root.path("result").path("content");
        if (!contentArray.isArray() || contentArray.isEmpty()) {
            throw new RuntimeException("MCP 返回格式错误：content 为空");
        }
        String innerText = contentArray.get(0).path("text").asText();

        // 解析内层 JSON（12306 的实际数据）
        JsonNode data = objectMapper.readTree(innerText);

        TrainQueryResponse response = new TrainQueryResponse();
        response.setFrom(data.path("from_station").asText());
        response.setTo(data.path("to_station").asText());
        response.setDate(data.path("train_date").asText());
        response.setCount(data.path("count").asInt());

        List<TrainQueryResponse.TrainItem> trains = new ArrayList<>();
        JsonNode dataArray = data.path("data");
        if (dataArray.isArray()) {
            for (JsonNode item : dataArray) {
                TrainQueryResponse.TrainItem t = new TrainQueryResponse.TrainItem();
                t.setTrainNo(item.path("train_no").asText());
                t.setTrainCode(item.path("train_code").asText());
                t.setFromStation(item.path("from_station").asText());
                t.setToStation(item.path("to_station").asText());
                t.setStartTime(item.path("start_time").asText());
                t.setArriveTime(item.path("arrive_time").asText());
                t.setDuration(item.path("duration").asText());
                t.setTrainClassName(item.path("train_class_name").asText());

                // 解析 prices
                Map<String, String> prices = new HashMap<>();
                JsonNode priceNode = item.path("prices");
                if (priceNode.isObject()) {
                    priceNode.fields().forEachRemaining(entry ->
                            prices.put(entry.getKey(), entry.getValue().asText()));
                }
                t.setPrices(prices);

                trains.add(t);
            }
        }
        response.setTrains(trains);

        return response;
    }
}