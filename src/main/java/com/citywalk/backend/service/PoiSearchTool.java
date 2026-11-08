package com.citywalk.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.dto.PoiInfo;
import com.citywalk.backend.entity.City;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.CityMapper;
import com.citywalk.backend.mapper.PoiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 智能导游的工具：查询本地 POI（景点表）。
 * 大模型在用户问「推荐景点 / 有什么好吃的 / 哪里好逛」时自动调用它。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PoiSearchTool {

    private final PoiMapper poiMapper;
    private final CityMapper cityMapper;

    @Tool(description = """
            根据城市名和关键词查询本地景点(POI)。
            当用户询问景点推荐、美食、购物、历史文化地点、公园等具体地点时调用，
            返回真实存在的地点名称、类别、地址、经纬度和简介。""")
    public List<PoiInfo> searchCityPOI(
            @ToolParam(description = "城市名称，例如：佛山、成都、西安", required = false) String cityName,
            @ToolParam(description = "搜索关键词，例如：美食、寺庙、公园、历史古迹、购物", required = false) String keyword,
            ToolContext toolContext) {

        Long cityId = resolveCityId(cityName, toolContext);
        if (cityId == null) {
            log.warn("AI 导游查 POI 时无法确定城市（cityName={}）", cityName);
            return List.of();
        }

        List<Poi> pois = queryPois(cityId, keyword);
        List<PoiInfo> result = toInfoList(pois);
        log.info("AI 导游工具 searchCityPOI 命中 {} 个 POI（cityId={}, keyword={}）", result.size(), cityId, keyword);

        // 把查到的 POI 记录进 toolContext，供控制器在 SSE 里回传给前端打点
        Object holder = toolContext.getContext().get("foundPois");
        if (holder instanceof Collection) {
            ((Collection) holder).addAll(result);
        }

        return result;
    }

    private Long resolveCityId(String cityName, ToolContext toolContext) {
        // 优先用控制器通过 toolContext 注入的 cityId（更可靠）
        Object ctxCityId = toolContext.getContext().get("cityId");
        if (ctxCityId instanceof Number) {
            return ((Number) ctxCityId).longValue();
        }
        if (ctxCityId instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // 其次按模型给出的城市名反查
        if (cityName != null && !cityName.isBlank()) {
            City city = cityMapper.selectOne(new LambdaQueryWrapper<City>()
                    .eq(City::getName, cityName.trim())
                    .last("LIMIT 1"));
            if (city != null) {
                return city.getId();
            }
        }
        return null;
    }

    private List<Poi> queryPois(Long cityId, String keyword) {
        LambdaQueryWrapper<Poi> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Poi::getCityId, cityId);
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(Poi::getName, k)
                    .or().like(Poi::getCategory, k)
                    .or().like(Poi::getDescription, k));
        }
        wrapper.last("LIMIT 10");
        return poiMapper.selectList(wrapper);
    }

    private List<PoiInfo> toInfoList(List<Poi> pois) {
        return pois.stream().map(p -> {
            PoiInfo info = new PoiInfo();
            info.setId(p.getId());
            info.setName(p.getName());
            info.setCategory(p.getCategory());
            info.setAddress(p.getAddress());
            info.setLat(p.getLat() != null ? p.getLat().doubleValue() : null);
            info.setLng(p.getLng() != null ? p.getLng().doubleValue() : null);
            info.setDescription(p.getDescription());
            return info;
        }).collect(Collectors.toList());
    }
}
