package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SaveDraftRequest {
    private Long cityId;
    private String title;
    private String theme;
    private Integer duration;
    private String difficulty;
    private Integer aiGenerated;
    private Long routeId;
    private List<Node> nodes;
    private List<Ticket> tickets;

    @Data
    public static class Node {
        private Long poiId;
        private Integer sortOrder;
        private Integer stayDuration;
        private String tip;
    }

    @Data
    public static class Ticket {
        private String trainCode;
        private String fromStation;
        private String toStation;
        private String startTime;
        private String arriveTime;
        private String travelDate;
        private Map<String, String> prices;
    }
}
