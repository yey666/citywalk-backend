package com.citywalk.backend.controller;

import com.citywalk.backend.dto.GuideChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Tag(name = "AI 导游", description = "AI 智能导游对话（SSE 流式输出）")
@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
public class GuideController {

    private final ChatModel chatModel;

    @Operation(summary = "AI 导游流式对话")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody GuideChatRequest request) {
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.isEmpty()) {
            return Flux.just(ServerSentEvent.builder("请输入想问的问题").build());
        }

        String cityName = request.getCityName();
        String prompt = buildPrompt(cityName, message);

        log.info("AI 导游提问：{}（城市：{}）", message, cityName);

        return ChatClient.builder(chatModel)
                .build()
                .prompt(prompt)
                .stream()
                .content()
                .map(chunk -> ServerSentEvent.builder(chunk).build())
                .onErrorResume(err -> {
                    log.error("AI 导游流式输出失败", err);
                    return Flux.just(ServerSentEvent.builder("[出错了，请稍后再试]").build());
                });
    }

    private String buildPrompt(String cityName, String userMessage) {
        String city = (cityName == null || cityName.isBlank()) ? "当前城市" : cityName;
        return String.format("""
                你是一名专业的 Citywalk 智能导游，性格热情、口语化，像一位熟悉本地的好朋友。
                当前城市：%s

                要求：
                1. 用简洁、生动、口语化的中文回答
                2. 回答尽量精炼，一般不超过 200 字
                3. 涉及景点、美食、路线时给出具体实用的建议

                用户问题：%s
                """, city, userMessage);
    }
}
