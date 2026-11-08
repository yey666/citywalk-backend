package com.citywalk.backend.controller;

import com.citywalk.backend.dto.GuideChatRequest;
import com.citywalk.backend.dto.PoiInfo;
import com.citywalk.backend.service.PoiSearchTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Tag(name = "AI 导游", description = "AI 智能导游对话（SSE 流式输出）")
@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
public class GuideController {

    /** 最多保留最近 10 轮对话（一轮 = 1 条 user + 1 条 assistant），防止 Token 爆炸 */
    private static final int MAX_HISTORY_MESSAGES = 20;

    private final ChatModel chatModel;
    private final PoiSearchTool poiSearchTool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "AI 导游流式对话（多轮上下文 + POI 工具）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody GuideChatRequest request) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(request.getCityName())));

        // 追加历史消息（只保留最近的 MAX_HISTORY_MESSAGES 条）
        List<GuideChatRequest.ChatMessage> history = request.getMessages();
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
            for (int i = start; i < history.size(); i++) {
                GuideChatRequest.ChatMessage m = history.get(i);
                String content = m.getContent() == null ? "" : m.getContent().trim();
                if (content.isEmpty()) {
                    continue;
                }
                if ("assistant".equalsIgnoreCase(m.getRole())) {
                    messages.add(new AssistantMessage(content));
                } else {
                    messages.add(new UserMessage(content));
                }
            }
        }

        boolean hasUserMessage = messages.stream().anyMatch(m -> m instanceof UserMessage);
        if (!hasUserMessage) {
            return Flux.just(ServerSentEvent.builder(jsonTextEvent("请输入想问的问题")).build());
        }

        Prompt prompt = new Prompt(messages);

        // 本次请求内，工具查到的 POI 会写入这里，供 SSE 结尾回传给前端打点
        List<PoiInfo> foundPois = new CopyOnWriteArrayList<>();
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("foundPois", foundPois);
        if (request.getCityId() != null) {
            toolContext.put("cityId", request.getCityId());
        }

        Flux<ServerSentEvent<String>> textEvents = ChatClient.builder(chatModel)
                .build()
                .prompt(prompt)
                .tools(poiSearchTool)
                .toolContext(toolContext)
                .stream()
                .content()
                .map(chunk -> ServerSentEvent.builder(jsonTextEvent(chunk)).build());

        // 文本流结束后，把工具查到的 POI 作为最后一个事件推给前端
        Flux<ServerSentEvent<String>> poisEvent = Flux.defer(() -> {
            if (foundPois.isEmpty()) {
                return Flux.empty();
            }
            return Flux.just(ServerSentEvent.builder(jsonPoisEvent(new ArrayList<>(foundPois))).build());
        });

        return Flux.concat(textEvents, poisEvent)
                .onErrorResume(err -> {
                    log.error("AI 导游流式输出失败", err);
                    return Flux.just(ServerSentEvent.builder(jsonTextEvent("[出错了，请稍后再试]")).build());
                });
    }

    private String buildSystemPrompt(String cityName) {
        String city = (cityName == null || cityName.isBlank()) ? "当前城市" : cityName;
        return String.format("""
                你是一名专业的 Citywalk 智能导游，性格热情、口语化，像一位熟悉本地的好朋友。
                当前城市：%s

                要求：
                1. 用简洁、生动、口语化的中文回答
                2. 回答尽量精炼，一般不超过 200 字
                3. 涉及景点、美食、路线等具体地点推荐时，先调用 searchCityPOI 工具查真实数据，再基于结果回答
                4. 结合上下文回答用户，记住对话中提到过的偏好
                """, city);
    }

    private String jsonTextEvent(String content) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "text");
        m.put("content", content);
        return writeJson(m);
    }

    private String jsonPoisEvent(List<PoiInfo> pois) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "pois");
        m.put("pois", pois);
        return writeJson(m);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error("SSE 事件 JSON 序列化失败", e);
            return "{}";
        }
    }
}
