package com.citywalk.backend.util;

import com.citywalk.backend.config.AmapConfig;
import com.citywalk.backend.dto.AmapPoiResponse;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.PoiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmapPoiFetcher {

    private final AmapConfig amapConfig;
    private final PoiMapper poiMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    // 要拉取的类别
    private static final String[] TYPES = {
            "110100",   // 风景名胜
            "110200",   // 公园广场
            "140000",   // 餐饮服务
    };

    // 佛山 adcode
    private static final String FOSHAN_ADCODE = "440600";

    /**
     * 拉取佛山的景点和餐厅
     */
    public int fetchFoshanPois(Long cityId) {
        int total = 0;
        for (String type : TYPES) {
            try {
                log.info("===== 开始拉取类别: {}", type);
                List<Poi> pois = fetchByType(type, cityId);
                log.info("===== 类别 {} 解析出 {} 个 POI", type, pois.size());

                for (Poi poi : pois) {
                    // 去重
                    Long count = poiMapper.selectCount(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Poi>()
                                    .eq(Poi::getCityId, cityId)
                                    .eq(Poi::getName, poi.getName())
                    );
                    if (count == 0) {
                        poiMapper.insert(poi);
                        total++;
                    }
                }
                log.info("===== 类别 {} 完成，累计入库 {} 条", type, total);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("===== 拉取类别 {} 失败", type, e);
            }
        }
        return total;
    }

    /**
     * 按类别拉取一个城市的所有 POI
     */
    private List<Poi> fetchByType(String type, Long cityId) throws Exception {
        String url = amapConfig.getBaseUrl() + "/v3/place/text"
                + "?key=" + amapConfig.getKey()
                + "&types=" + type
                + "&city=" + FOSHAN_ADCODE
                + "&citylimit=true"
                + "&offset=25&page=1&extensions=all";

        log.info("===== 请求 URL: {}", url);

        AmapPoiResponse response = restTemplate.getForObject(url, AmapPoiResponse.class);

        log.info("===== 高德返回: status={}, info={}, count={}, pois={}",
                response != null ? response.getStatus() : "null",
                response != null ? response.getInfo() : "null",
                response != null ? response.getCount() : "null",
                response != null && response.getPois() != null ? response.getPois().size() : "null");

        List<Poi> result = new ArrayList<>();
        if (response == null || !"1".equals(response.getStatus())
                || response.getPois() == null) {
            log.warn("===== 响应无效，直接返回空");
            return result;
        }

        for (AmapPoiResponse.PoiItem item : response.getPois()) {
            log.info("===== POI: name={}, location={}, type={}",
                    item.getName(), item.getLocation(), item.getType());

            if (item.getLocation() == null || !item.getLocation().contains(",")) {
                log.warn("===== 跳过无效 location: {}", item.getName());
                continue;
            }

            try {
                String[] lngLat = item.getLocation().split(",");

                Poi poi = new Poi();
                poi.setCityId(cityId);
                poi.setName(item.getName());
                poi.setCategory(item.getType() != null && item.getType().contains(";")
                        ? item.getType().split(";")[0]
                        : item.getType());
                poi.setAddress(item.getAddress());
                poi.setLng(new BigDecimal(lngLat[0].trim()));
                poi.setLat(new BigDecimal(lngLat[1].trim()));
                poi.setPhotoScore(3);
                poi.setFoodScore(3);
                poi.setHistoryScore(3);
                poi.setCultureScore(3);
                poi.setPriceLevel(0);
                poi.setDescription("");

                result.add(poi);
            } catch (Exception e) {
                log.error("===== 解析 POI 失败: name={}, location={}", item.getName(), item.getLocation(), e);
            }
        }

        return result;
    }
}