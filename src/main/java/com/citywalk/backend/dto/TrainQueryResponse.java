package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TrainQueryResponse {
    private String from;
    private String to;
    private String date;
    private Integer count;
    private List<TrainItem> trains;

    @Data
    public static class TrainItem {
        private String trainNo;      // 内部车次号
        private String trainCode;    // 车次代码（如 G1556）
        private String fromStation;
        private String toStation;
        private String startTime;
        private String arriveTime;
        private String duration;
        private String trainClassName;
        private Map<String, String> prices;  // 席别 -> 价格
    }
}