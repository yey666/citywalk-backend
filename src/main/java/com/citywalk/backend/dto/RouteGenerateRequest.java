package com.citywalk.backend.dto;

import lombok.Data;

@Data
public class RouteGenerateRequest {
    private Long cityId;        // 城市 ID，如 1=苏州
    private Integer duration;   // 时长（小时），如 4
    private String theme;       // 主题：出片/美食/历史/文艺
    private String difficulty;  // 体力：轻松/中等/暴走
}