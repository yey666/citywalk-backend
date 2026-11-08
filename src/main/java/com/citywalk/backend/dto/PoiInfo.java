package com.citywalk.backend.dto;

import lombok.Data;

/**
 * AI 导游工具返回的 POI 简要信息（含经纬度，供前端地图打点）
 */
@Data
public class PoiInfo {
    private Long id;
    private String name;
    private String category;
    private String address;
    private Double lat;
    private Double lng;
    private String description;
}
