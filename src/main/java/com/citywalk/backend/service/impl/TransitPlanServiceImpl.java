package com.citywalk.backend.service.impl;

import com.citywalk.backend.dto.TransitPlanResponse;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.entity.RouteNode;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.mapper.RouteMapper;
import com.citywalk.backend.mapper.RouteNodeMapper;
import com.citywalk.backend.service.TransitPlanService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransitPlanServiceImpl implements TransitPlanService {

    private final RouteMapper routeMapper;
    private final RouteNodeMapper routeNodeMapper;
    private final PoiMapper poiMapper;

    @Value("${amap.key}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TransitPlanResponse planByRoute(Long routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new RuntimeException("路线不存在");
        }

        // 查路线所有节点（按顺序）
        List<RouteNode> nodes = routeNodeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteNode>()
                        .eq(RouteNode::getRouteId, routeId)
                        .orderByAsc(RouteNode::getSortOrder)
        );

        TransitPlanResponse response = new TransitPlanResponse();
        response.setRouteId(routeId);
        response.setRouteTitle(route.getTitle());
        List<TransitPlanResponse.SegmentItem> segments = new ArrayList<>();

        // 遍历相邻节点
        for (int i = 0; i < nodes.size() - 1; i++) {
            RouteNode fromNode = nodes.get(i);
            RouteNode toNode = nodes.get(i + 1);

            Poi fromPoi = poiMapper.selectById(fromNode.getPoiId());
            Poi toPoi = poiMapper.selectById(toNode.getPoiId());
            if (fromPoi == null || toPoi == null) {
                continue;
            }

            try {
                TransitPlanResponse.SegmentItem segment = callAmapTransit(
                        fromPoi.getLng() + "," + fromPoi.getLat(),
                        toPoi.getLng() + "," + toPoi.getLat(),
                        fromPoi.getName(),
                        toPoi.getName()
                );
                segments.add(segment);
                Thread.sleep(200); // 防止限流
            } catch (Exception e) {
                log.error("查询公交方案失败: {} -> {}", fromPoi.getName(), toPoi.getName(), e);
            }
        }

        response.setSegments(segments);
        return response;
    }

    private TransitPlanResponse.SegmentItem callAmapTransit(String origin, String destination,
                                                            String fromName, String toName) throws Exception {
        String url = String.format(
                "https://restapi.amap.com/v5/direction/transit/integrated" +
                        "?key=%s&origin=%s&destination=%s&city1=0512&city2=0512&strategy=0",
                amapKey, origin, destination
        );

        String body = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(body);

        if (!"1".equals(root.path("status").asText())) {
            throw new RuntimeException("高德返回错误: " + root.path("info").asText());
        }

        TransitPlanResponse.SegmentItem item = new TransitPlanResponse.SegmentItem();
        item.setFromPoiName(fromName);
        item.setToPoiName(toName);

        JsonNode transits = root.path("route").path("transits");
        if (transits.isArray() && transits.size() > 0) {
            JsonNode firstTransit = transits.get(0);
            item.setDistance(firstTransit.path("distance").asText());
            item.setWalkingDistance(firstTransit.path("walking_distance").asText());

            List<TransitPlanResponse.StepItem> steps = new ArrayList<>();
            JsonNode segments = firstTransit.path("segments");
            for (JsonNode segment : segments) {
                // 处理步行段
                JsonNode walking = segment.path("walking");
                if (!walking.isMissingNode()) {
                    TransitPlanResponse.StepItem step = new TransitPlanResponse.StepItem();
                    step.setType("walk");
                    step.setDistance(walking.path("distance").asText());
                    // 拼接所有 instruction
                    StringBuilder sb = new StringBuilder();
                    JsonNode stepArray = walking.path("steps");
                    for (JsonNode s : stepArray) {
                        sb.append(s.path("instruction").asText()).append("；");
                    }
                    step.setInstruction(sb.toString());
                    steps.add(step);
                }

                // 处理公交段
                JsonNode bus = segment.path("bus");
                if (!bus.isMissingNode()) {
                    JsonNode buslines = bus.path("buslines");
                    if (buslines.isArray() && buslines.size() > 0) {
                        JsonNode line = buslines.get(0);
                        TransitPlanResponse.StepItem step = new TransitPlanResponse.StepItem();
                        step.setType("bus");
                        step.setLineName(line.path("name").asText());
                        step.setDepartureStop(line.path("departure_stop").path("name").asText());
                        step.setArrivalStop(line.path("arrival_stop").path("name").asText());
                        step.setDistance(line.path("distance").asText());
                        steps.add(step);
                    }
                }
            }
            item.setSteps(steps);
        }

        return item;
    }
}