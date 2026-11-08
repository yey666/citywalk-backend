package com.citywalk.backend.dto;

import lombok.Data;

@Data
public class GuideChatRequest {
    private String message;    // 用户提问
    private String cityName;   // 当前城市名（可选，用于注入上下文）
}
