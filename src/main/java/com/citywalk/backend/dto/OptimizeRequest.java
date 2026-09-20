package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class OptimizeRequest {
    private List<Node> nodes;

    @Data
    public static class Node {
        private Long poiId;
        private String name;
        private Double lat;
        private Double lng;
        private Integer stayDuration;
        private String tip;
    }
}