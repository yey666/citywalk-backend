package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

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
    private List<TicketDetail> tickets;

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

    @Data
    public static class TicketDetail {
        private String trainCode;
        private String fromStation;
        private String toStation;
        private String startTime;
        private String arriveTime;
        private String travelDate;
        private Map<String, String> prices;
    }
}
