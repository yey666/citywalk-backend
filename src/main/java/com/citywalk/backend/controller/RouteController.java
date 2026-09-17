package com.citywalk.backend.controller;

import com.citywalk.backend.dto.RouteDetailVO;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.service.RouteService;
import com.citywalk.backend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "路线", description = "城市路线列表、路线详情、保存、我的路线")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "查询城市官方路线列表")
    @GetMapping("/city/{cityId}/routes")
    public List<Route> listByCity(@PathVariable Long cityId) {
        return routeService.listByCity(cityId);
    }

    @Operation(summary = "查询路线详情")
    @GetMapping("/route/{id}")
    public RouteDetailVO detail(@PathVariable Long id) {
        return routeService.getDetail(id);
    }

    @Operation(summary = "保存路线到我的收藏")
    @PostMapping("/route/{id}/save")
    public Map<String, Object> saveRoute(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        boolean success = routeService.saveRoute(userId, id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("routeId", id);
        return result;
    }

    @Operation(summary = "查询我保存的路线")
    @GetMapping("/user/routes")
    public List<Route> myRoutes() {
        Long userId = UserContext.getUserId();
        return routeService.listByUser(userId);
    }
}