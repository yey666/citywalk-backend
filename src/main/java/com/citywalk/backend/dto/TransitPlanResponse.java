package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransitPlanResponse {
    private Long routeId;
    private String routeTitle;
    private List<SegmentItem> segments;  // 相邻节点间的方案

    @Data
    public static class SegmentItem {
        private String fromPoiName;
        private String toPoiName;
        private String distance;        // 总距离（米）
        private String walkingDistance; // 步行距离（米）
        private List<StepItem> steps;   // 具体的步行/公交步骤
    }

    @Data
    public static class StepItem {
        private String type;        // "walk" / "bus"
        private String instruction; // 步行指令，或公交线路名
        private String distance;    // 距离
        private String lineName;    // 公交线路名
        private String departureStop; // 上车站
        private String arrivalStop;   // 下车站
        private Integer stopCount;    // 站数
    }
}