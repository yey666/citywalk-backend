package com.citywalk.backend.service;

import com.citywalk.backend.dto.TransitPlanResponse;

public interface TransitPlanService {

    TransitPlanResponse planByRoute(Long routeId);
}