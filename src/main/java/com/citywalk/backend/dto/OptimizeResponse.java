package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class OptimizeResponse {
    private List<OptimizeRequest.Node> optimizedNodes;  // ← 必须是 OptimizeRequest.Node
    private Double originalDistance;
    private Double optimizedDistance;
    private Double savedDistance;
}