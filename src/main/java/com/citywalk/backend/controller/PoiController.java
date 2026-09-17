package com.citywalk.backend.controller;

import com.citywalk.backend.util.AmapPoiFetcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "兴趣点", description = "POI 数据拉取")
@RestController
@RequestMapping("/api/poi")
@RequiredArgsConstructor
public class PoiController {

    private final AmapPoiFetcher amapPoiFetcher;

    @Operation(summary = "拉取 POI 数据")
    @GetMapping("/fetch")
    public Map<String, Object> fetch(@RequestParam Long cityId) {
        int success = amapPoiFetcher.fetchSuzhouPois(cityId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("total", 30);
        return result;
    }
}