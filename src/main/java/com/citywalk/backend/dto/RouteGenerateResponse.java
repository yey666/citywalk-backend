package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteGenerateResponse {
    private String title;
    private String theme;
    private Integer duration;
    private String difficulty;
    private List<RouteNode> nodes;

    @Data
    public static class RouteNode {
        private Integer order;         // 节点顺序
        private Long poiId;            // POI ID
        private String poiName;        // POI 名称
        private Integer stayDuration;  // 停留时间（分钟）
        private String tip;            // 本地人提示
        private String photoSpot;      // 机位描述
        private Double lat;            // 纬度
        private Double lng;            // 经度
    }
}