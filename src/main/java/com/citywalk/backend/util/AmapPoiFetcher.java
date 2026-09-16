package com.citywalk.backend.util;

import com.citywalk.backend.config.AmapConfig;
import com.citywalk.backend.dto.AmapPoiResponse;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.PoiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmapPoiFetcher {

    private final AmapConfig amapConfig;

    private final PoiMapper poiMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 苏州 30 个 POI 名称（第一轮占位数据）
     */
    private static final String[] SUZHOU_POIS = {
            "拙政园", "狮子林", "苏州博物馆", "平江路", "山塘街", "观前街",
            "网师园", "沧浪亭", "十全街", "双塔市集", "定慧寺巷", "艺圃",
            "耦园", "怡园", "可园", "苏州美术馆", "苏州丝绸博物馆",
            "金鸡湖", "诚品书店", "苏州中心", "李公堤", "斜塘老街",
            "独墅湖教堂", "白塘生态植物园", "苏州大学",
            "同得兴", "哑巴生煎", "松鹤楼", "得月楼", "三万昌茶馆"
    };

    /**
     * 拉取苏州 30 个 POI 并存入数据库
     * @param cityId 苏州在 city 表里的 id（你数据库里是 1）
     * @return 成功入库数量
     */
    public int fetchSuzhouPois(Long cityId) {
        int success = 0;
        for (String keyword : SUZHOU_POIS) {
            try {
                // 先查是否已存在
                Long count = poiMapper.selectCount(
                        new LambdaQueryWrapper<Poi>()
                                .eq(Poi::getCityId, cityId)
                                .eq(Poi::getName, keyword)
                );
                if (count > 0) {
                    log.info("已存在，跳过：{}", keyword);
                    continue;
                }

                Poi poi = fetchOne(keyword, cityId);
                if (poi != null) {
                    poiMapper.insert(poi);
                    success++;
                    log.info("成功入库：{}", keyword);
                } else {
                    log.warn("未找到：{}", keyword);
                }
                Thread.sleep(300);
            } catch (Exception e) {
                log.error("拉取 {} 失败：{}", keyword, e.getMessage());
            }
        }
        return success;
    }

    /**
     * 调高德 API 查一个 POI
     */
    private Poi fetchOne(String keyword, Long cityId) throws Exception {
        String url = amapConfig.getBaseUrl() + "/v3/place/text"
                + "?key=" + amapConfig.getKey()
                + "&keywords=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                + "&city=" + URLEncoder.encode("苏州", StandardCharsets.UTF_8)
                + "&offset=1&page=1&extensions=all";

        AmapPoiResponse response = restTemplate.getForObject(url, AmapPoiResponse.class);

        if (response == null || !"1".equals(response.getStatus())
                || response.getPois() == null || response.getPois().isEmpty()) {
            return null;
        }

        AmapPoiResponse.PoiItem item = response.getPois().get(0);

        // location 格式："经度,纬度"
        String[] lngLat = item.getLocation().split(",");
        BigDecimal lng = new BigDecimal(lngLat[0]);
        BigDecimal lat = new BigDecimal(lngLat[1]);

        Poi poi = new Poi();
        poi.setCityId(cityId);
        poi.setName(item.getName());
        poi.setCategory(item.getType() != null && item.getType().contains(";")
                ? item.getType().split(";")[0]
                : item.getType());
        poi.setAddress(item.getAddress());
        poi.setLat(lat);
        poi.setLng(lng);
        poi.setPhotoScore(3);
        poi.setFoodScore(3);
        poi.setHistoryScore(3);
        poi.setCultureScore(3);
        poi.setPriceLevel(0);
        poi.setDescription("");

        return poi;
    }
}