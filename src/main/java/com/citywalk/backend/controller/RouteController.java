package com.citywalk.backend.controller;

import com.citywalk.backend.dto.RouteDetailVO;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * 城市官方路线列表
     * GET /api/city/1/routes
     */
    @GetMapping("/city/{cityId}/routes")
    public List<Route> listByCity(@PathVariable Long cityId) {
        return routeService.listByCity(cityId);
    }

    /**
     * 路线详情
     * GET /api/route/1
     */
    @GetMapping("/route/{id}")
    public RouteDetailVO detail(@PathVariable Long id) {
        return routeService.getDetail(id);
    }
}