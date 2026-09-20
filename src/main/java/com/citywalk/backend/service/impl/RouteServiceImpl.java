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
    }}