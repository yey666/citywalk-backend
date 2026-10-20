package com.citywalk.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.dto.PoiBriefVO;
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
import java.util.stream.Collectors;

@Tag(name = "POI", description = "POI 数据拉取")
@RestController
@RequestMapping("/api/poi")
@RequiredArgsConstructor
public class PoiController {

    private final AmapPoiFetcher amapPoiFetcher;
    private final PoiMapper poiMapper;

    @Operation(summary = "拉取 POI 数据")
    @GetMapping("/fetch")
    public Map<String, Object> fetch(@RequestParam Long cityId,
                                     @RequestParam(defaultValue = "0") Integer debug) {
        Map<String, Object> result = new HashMap<>();
        if (debug != null && debug == 1) {
            result.put("debug", amapPoiFetcher.fetchFirstPoiRaw());
            return result;
        }
        int success = amapPoiFetcher.fetchFoshanPois(cityId);
        result.put("success", success);
        return result;
    }

    @Operation(summary = "专门拉取餐饮服务 POI（美食主题数据源）")
    @GetMapping("/fetch-food")
    public Map<String, Object> fetchFood(@RequestParam Long cityId) {
        int success = amapPoiFetcher.fetchFoodPois(cityId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    @Operation(summary = "补数据：回填 photos / openHours（每次最多 20 条）")
    @PostMapping("/refresh-photos")
    public Map<String, Object> refreshPhotos(@RequestParam Long cityId) {
        int updated = amapPoiFetcher.refreshPhotos(cityId);
        Map<String, Object> result = new HashMap<>();
        result.put("updated", updated);
        return result;
    }

    @Operation(summary = "回填高德 POI ID（每次最多 20 条）")
    @GetMapping("/backfill-amap-id")
    public Map<String, Object> backfillAmapId(@RequestParam Long cityId) {
        int updated = amapPoiFetcher.backfillAmapId(cityId);
        Map<String, Object> result = new HashMap<>();
        result.put("updated", updated);
        return result;
    }

    @Operation(summary = "查询城市的 POI 列表")
    @GetMapping("/by-city/{cityId}")
    public List<Poi> listByCity(@PathVariable Long cityId) {
        return poiMapper.selectList(
                new LambdaQueryWrapper<Poi>().eq(Poi::getCityId, cityId)
        );
    }

    @Operation(summary = "按关键字搜索 POI（发帖关联用）")
    @GetMapping("/search")
    public List<PoiBriefVO> search(@RequestParam String keyword) {
        return poiMapper.selectList(
                new LambdaQueryWrapper<Poi>().like(Poi::getName, keyword)
        ).stream().map(poi -> {
            PoiBriefVO vo = new PoiBriefVO();
            vo.setId(poi.getId());
            vo.setName(poi.getName());
            vo.setCityId(poi.getCityId());
            return vo;
        }).collect(Collectors.toList());
    }
}