package com.citywalk.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.dto.RouteDetailVO;
import com.citywalk.backend.dto.SaveDraftRequest;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.entity.RouteNode;
import com.citywalk.backend.entity.UserRoute;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.mapper.RouteMapper;
import com.citywalk.backend.mapper.RouteNodeMapper;
import com.citywalk.backend.mapper.UserRouteMapper;
import com.citywalk.backend.service.RouteService;
import com.citywalk.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.citywalk.backend.dto.OptimizeRequest;
import com.citywalk.backend.dto.OptimizeResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteMapper routeMapper;
    private final RouteNodeMapper routeNodeMapper;
    private final PoiMapper poiMapper;
    private final UserRouteMapper userRouteMapper;

    @Override
    public List<Route> listByCity(Long cityId) {
        return routeMapper.selectList(
                new LambdaQueryWrapper<Route>()
                        .eq(Route::getCityId, cityId)
                        .eq(Route::getStatus, "official")
                        .orderByDesc(Route::getId)
        );
    }

    @Override
    public RouteDetailVO getDetail(Long routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new RuntimeException("路线不存在");
        }

        RouteDetailVO vo = new RouteDetailVO();
        vo.setId(route.getId());
        vo.setCityId(route.getCityId());
        vo.setTitle(route.getTitle());
        vo.setTheme(route.getTheme());
        vo.setDuration(route.getDuration());
        vo.setDifficulty(route.getDifficulty());
        vo.setBestTime(route.getBestTime());
        vo.setDescription(route.getDescription());

        // 查节点
        List<RouteNode> nodes = routeNodeMapper.selectList(
                new LambdaQueryWrapper<RouteNode>()
                        .eq(RouteNode::getRouteId, routeId)
                        .orderByAsc(RouteNode::getSortOrder)
        );

        List<RouteDetailVO.NodeDetail> nodeDetails = new ArrayList<>();
        for (RouteNode node : nodes) {
            RouteDetailVO.NodeDetail detail = new RouteDetailVO.NodeDetail();
            detail.setOrder(node.getSortOrder());
            detail.setPoiId(node.getPoiId());
            detail.setStayDuration(node.getStayDuration());
            detail.setTip(node.getTip());
            detail.setPhotoSpot(node.getPhotoSpot());

            // 补 POI 信息
            Poi poi = poiMapper.selectById(node.getPoiId());
            if (poi != null) {
                detail.setPoiName(poi.getName());
                detail.setCategory(poi.getCategory());
                detail.setAddress(poi.getAddress());
                detail.setLat(poi.getLat() != null ? poi.getLat().doubleValue() : null);
                detail.setLng(poi.getLng() != null ? poi.getLng().doubleValue() : null);
            }
            nodeDetails.add(detail);
        }
        vo.setNodes(nodeDetails);

        return vo;
    }

    @Override
    public boolean saveRoute(Long userId, Long routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new RuntimeException("路线不存在");
        }

        Long count = userRouteMapper.selectCount(
                new LambdaQueryWrapper<UserRoute>()
                        .eq(UserRoute::getUserId, userId)
                        .eq(UserRoute::getRouteId, routeId)
        );
        if (count > 0) {
            throw new RuntimeException("已收藏该路线");
        }

        UserRoute ur = new UserRoute();
        ur.setUserId(userId);
        ur.setRouteId(routeId);
        userRouteMapper.insert(ur);

        return true;
    }

    @Override
    public List<Route> listByUser(Long userId) {
        List<UserRoute> userRoutes = userRouteMapper.selectList(
                new LambdaQueryWrapper<UserRoute>()
                        .eq(UserRoute::getUserId, userId)
                        .orderByDesc(UserRoute::getCreatedAt)
        );

        if (userRoutes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> routeIds = userRoutes.stream()
                .map(UserRoute::getRouteId)
                .collect(Collectors.toList());

        return routeMapper.selectBatchIds(routeIds);
    }

    @Override
    @Transactional
    public Long saveDraft(SaveDraftRequest request) {
        Long userId = UserContext.getUserId();

        // 1. 幂等校验
        Route existing = routeMapper.selectOne(
                new LambdaQueryWrapper<Route>()
                        .eq(Route::getUserId, userId)
                        .eq(Route::getCityId, request.getCityId())
                        .eq(Route::getStatus, "saved")
                        .eq(Route::getTitle, request.getTitle())
                        .orderByDesc(Route::getId)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            return existing.getId();
        }

        // 2. 保存 route
        Route route = new Route();
        route.setCityId(request.getCityId());
        route.setTitle(request.getTitle());
        route.setTheme(request.getTheme());
        route.setDuration(request.getDuration());
        route.setDifficulty(request.getDifficulty());
        route.setStatus("saved");
        route.setAiGenerated(0);
        route.setUserId(userId);
        routeMapper.insert(route);

        // 3. 保存 route_node
        for (SaveDraftRequest.Node node : request.getNodes()) {
            RouteNode rn = new RouteNode();
            rn.setRouteId(route.getId());
            rn.setPoiId(node.getPoiId());
            rn.setSortOrder(node.getSortOrder());
            rn.setStayDuration(node.getStayDuration());
            rn.setTip(node.getTip());
            routeNodeMapper.insert(rn);
        }

        return route.getId();
    }
    @Override
    public OptimizeResponse optimizeDraft(OptimizeRequest request) {
        List<OptimizeRequest.Node> nodes = request.getNodes();

        if (nodes == null || nodes.size() <= 1) {
            OptimizeResponse response = new OptimizeResponse();
            response.setOptimizedNodes(nodes);
            response.setOriginalDistance(0.0);
            response.setOptimizedDistance(0.0);
            response.setSavedDistance(0.0);
            return response;
        }

        // 1. 计算原始总距离
        double originalDistance = totalDistance(nodes);

        // 2. 优化
        List<OptimizeRequest.Node> optimized;
        if (nodes.size() <= 6) {
            // 暴力枚举（固定第一个点）
            optimized = bruteForceOptimize(nodes);
        } else {
            // 贪心（固定第一个点）
            optimized = greedyOptimize(nodes);
        }

        // 3. 计算优化后总距离
        double optimizedDistance = totalDistance(optimized);

        OptimizeResponse response = new OptimizeResponse();
        response.setOptimizedNodes(optimized);
        response.setOriginalDistance(Math.round(originalDistance * 100) / 100.0);
        response.setOptimizedDistance(Math.round(optimizedDistance * 100) / 100.0);
        response.setSavedDistance(Math.round((originalDistance - optimizedDistance) * 100) / 100.0);
        return response;
    }

    /**
     * 计算一条路线的总距离
     */
    private double totalDistance(List<OptimizeRequest.Node> nodes) {
        double total = 0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            total += haversine(
                    nodes.get(i).getLat(), nodes.get(i).getLng(),
                    nodes.get(i + 1).getLat(), nodes.get(i + 1).getLng()
            );
        }
        return total;
    }

    /**
     * Haversine 公式：计算两点球面距离（km）
     */
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 暴力枚举：固定第一个点，全排列其他点，选总距离最短的
     */
    private List<OptimizeRequest.Node> bruteForceOptimize(List<OptimizeRequest.Node> nodes) {
        OptimizeRequest.Node first = nodes.get(0);
        List<OptimizeRequest.Node> rest = new ArrayList<>(nodes.subList(1, nodes.size()));

        List<OptimizeRequest.Node> best = null;
        double bestDistance = Double.MAX_VALUE;

        // 生成所有排列
        List<List<OptimizeRequest.Node>> permutations = new ArrayList<>();
        permute(rest, 0, permutations);

        for (List<OptimizeRequest.Node> perm : permutations) {
            List<OptimizeRequest.Node> candidate = new ArrayList<>();
            candidate.add(first);
            candidate.addAll(perm);
            double d = totalDistance(candidate);
            if (d < bestDistance) {
                bestDistance = d;
                best = candidate;
            }
        }

        return best != null ? best : nodes;
    }

    /**
     * 生成所有排列（递归）
     */
    private void permute(List<OptimizeRequest.Node> list, int start, List<List<OptimizeRequest.Node>> result) {
        if (start == list.size() - 1) {
            result.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            Collections.swap(list, start, i);
            permute(list, start + 1, result);
            Collections.swap(list, start, i);
        }
    }

    /**
     * 贪心算法：固定第一个点，每次选最近的未访问点
     */
    private List<OptimizeRequest.Node> greedyOptimize(List<OptimizeRequest.Node> nodes) {
        List<OptimizeRequest.Node> result = new ArrayList<>();
        List<OptimizeRequest.Node> remaining = new ArrayList<>(nodes);

        // 第一个点固定
        OptimizeRequest.Node current = remaining.remove(0);
        result.add(current);

        while (!remaining.isEmpty()) {
            // 找离 current 最近的
            OptimizeRequest.Node nearest = null;
            double minDist = Double.MAX_VALUE;
            for (OptimizeRequest.Node node : remaining) {
                double d = haversine(
                        current.getLat(), current.getLng(),
                        node.getLat(), node.getLng()
                );
                if (d < minDist) {
                    minDist = d;
                    nearest = node;
                }
            }
            result.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        return result;
    }
}