package com.citywalk.backend.service;

import com.citywalk.backend.dto.RouteDetailVO;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.dto.SaveDraftRequest;
import com.citywalk.backend.dto.OptimizeRequest;
import com.citywalk.backend.dto.OptimizeResponse;




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
    boolean saveRoute(Long userId, Long routeId);
    Long saveDraft(SaveDraftRequest request);
    OptimizeResponse optimizeDraft(OptimizeRequest request);
    List<Route> listByUser(Long userId);
    List<Route> listMyPlans(Long userId);
    void deleteRoute(Long routeId, Long userId);
}
