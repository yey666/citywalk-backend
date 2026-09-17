package com.citywalk.backend.service;

import com.citywalk.backend.dto.RouteGenerateRequest;
import com.citywalk.backend.dto.RouteGenerateResponse;

public interface RouteGenerateService {

    /**
     * 根据用户偏好，用 RAG + LLM 生成一条路线
     */
    RouteGenerateResponse generate(RouteGenerateRequest request);
}