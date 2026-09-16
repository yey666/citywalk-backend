package com.citywalk.backend.controller;

import com.citywalk.backend.util.AmapPoiFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/poi")
@RequiredArgsConstructor
public class PoiController {

    private final AmapPoiFetcher amapPoiFetcher;

    /**
     * 触发拉取苏州 POI
     * 访问 http://localhost:8080/api/poi/fetch?cityId=1
     */
    @GetMapping("/fetch")
    public Map<String, Object> fetch(@RequestParam Long cityId) {
        int success = amapPoiFetcher.fetchSuzhouPois(cityId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("total", 30);
        return result;
    }
}