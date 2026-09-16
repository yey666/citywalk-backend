package com.citywalk.backend.service;

public interface PoiVectorService {

    /**
     * 把所有 POI 向量化存入 Redis Stack
     * @return 成功处理的 POI 数量
     */
    int initPoiVectors();
}