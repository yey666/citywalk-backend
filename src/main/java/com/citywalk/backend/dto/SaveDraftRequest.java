package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveDraftRequest {
    private Long cityId;
    private String title;
    private String theme;
    private Integer duration;
    private String difficulty;
    private List<Node> nodes;

    @Data
    public static class Node {
        private Long poiId;
        private Integer sortOrder;
        private Integer stayDuration;
        private String tip;
    }
}