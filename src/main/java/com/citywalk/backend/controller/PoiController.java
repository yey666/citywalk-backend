package com.citywalk.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.util.AmapPoiFetcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "POI", description = "POI 数据拉取")
@RestController
@RequestMapping("/api/poi")
@RequiredArgsConstructor
public class PoiController {

    private final AmapPoiFetcher amapPoiFetcher;
    private final PoiMapper poiMapper;

    @Operation(summary = "拉取 POI 数据")
    @GetMapping("/fetch")
    public Map<String, Object> fetch(@RequestParam Long cityId) {
        int success = amapPoiFetcher.fetchFoshanPois(cityId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    @Operation(summary = "查询城市的 POI 列表")
    @GetMapping("/by-city/{cityId}")
    public List<Poi> listByCity(@PathVariable Long cityId) {
        return poiMapper.selectList(
                new LambdaQueryWrapper<Poi>().eq(Poi::getCityId, cityId)
        );
    }
}