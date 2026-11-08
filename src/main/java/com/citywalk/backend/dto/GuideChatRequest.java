package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class GuideChatRequest {
    private Long cityId;                     // 当前城市 ID（供工具查询使用）
    private String cityName;                 // 当前城市名（可选，用于注入上下文）
    private List<ChatMessage> messages;      // 对话历史（含当前用户提问，最后一条应为 user）

    @Data
    public static class ChatMessage {
        private String role;     // "user" | "assistant"
        private String content;
    }
}
