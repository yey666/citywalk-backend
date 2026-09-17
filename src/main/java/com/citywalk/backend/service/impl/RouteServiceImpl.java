package com.citywalk.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.dto.RouteDetailVO;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.entity.RouteNode;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.mapper.RouteMapper;
import com.citywalk.backend.mapper.RouteNodeMapper;
import com.citywalk.backend.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteMapper routeMapper;
    private final RouteNodeMapper routeNodeMapper;
    private final PoiMapper poiMapper;

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
}