package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteDetailVO {
    private Long id;
    private Long cityId;
    private String title;
    private String theme;
    private Integer duration;
    private String difficulty;
    private String bestTime;
    private String description;
    private List<NodeDetail> nodes;

    @Data
    public static class NodeDetail {
        private Integer order;
        private Long poiId;
        private String poiName;
        private String category;
        private String address;
        private Double lat;
        private Double lng;
        private Integer stayDuration;
        private String tip;
        private String photoSpot;
        private String avoidTip;
        private String restaurant;
        private String bestVisitTime;
        private Integer photoScore;
        private List<String> photos;
    }
}