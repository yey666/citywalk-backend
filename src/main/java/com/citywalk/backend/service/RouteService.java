package com.citywalk.backend.service;

import com.citywalk.backend.dto.RouteDetailVO;
import com.citywalk.backend.entity.Route;

import java.util.List;

public interface RouteService {

    /**
     * 查询某个城市的官方路线列表
     */
    List<Route> listByCity(Long cityId);

    /**
     * 查询路线详情（含节点）
     */
    RouteDetailVO getDetail(Long routeId);
}