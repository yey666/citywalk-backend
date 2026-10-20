package com.citywalk.backend.util;

import com.citywalk.backend.config.AmapConfig;
import com.citywalk.backend.dto.AmapPoiResponse;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.mapper.PoiMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 要拉取的景点类别（注意：高德编码 05=餐饮服务，14=科教文化服务；此前 140000 被误标为“餐饮服务”，实际拉的是文化广场/宗祠）
    private static final String[] TYPES = {
            "110100",   // 风景名胜
            "110200",   // 公园广场
    };

    // 餐饮服务类别（美食主题专用，单独拉取）
    private static final String[] FOOD_TYPES = {
            "050000",   // 餐饮服务
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
                total += savePois(pois, cityId);
                log.info("===== 类别 {} 完成，累计入库 {} 条", type, total);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("===== 拉取类别 {} 失败", type, e);
            }
        }
        return total;
    }

    /**
     * 专门拉取「餐饮服务」类别的 POI（美食主题数据源）
     */
    public int fetchFoodPois(Long cityId) {
        int total = 0;
        for (String type : FOOD_TYPES) {
            try {
                log.info("===== 开始拉取餐饮类别: {}", type);
                List<Poi> pois = fetchByType(type, cityId);
                log.info("===== 餐饮类别 {} 解析出 {} 个 POI", type, pois.size());
                total += savePois(pois, cityId);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("===== 拉取餐饮类别 {} 失败", type, e);
            }
        }
        return total;
    }

    /** 去重入库（by cityId + name） */
    private int savePois(List<Poi> pois, Long cityId) {
        int saved = 0;
        for (Poi poi : pois) {
            Long count = poiMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Poi>()
                            .eq(Poi::getCityId, cityId)
                            .eq(Poi::getName, poi.getName())
            );
            if (count == 0) {
                poiMapper.insert(poi);
                saved++;
            }
        }
        return saved;
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
                poi.setAmapId(item.getId());
                String rawType = item.getType() != null && item.getType().contains(";")
                        ? item.getType().split(";")[0]
                        : item.getType();
                poi.setCategory(mapCategory(rawType));
                poi.setAddress(item.getAddress());
                poi.setLng(new BigDecimal(lngLat[0].trim()));
                poi.setLat(new BigDecimal(lngLat[1].trim()));
                poi.setPhotoScore(3);
                poi.setFoodScore(3);
                poi.setHistoryScore(3);
                poi.setCultureScore(3);
                poi.setPriceLevel(0);
                poi.setDescription("");
                poi.setOpenHours(item.getOpentime2());
                if (item.getPhotos() != null && !item.getPhotos().isEmpty()) {
                    poi.setPhotos(objectMapper.writeValueAsString(item.getPhotos()));
                }

                result.add(poi);
            } catch (Exception e) {
                log.error("===== 解析 POI 失败: name={}, location={}", item.getName(), item.getLocation(), e);
            }
        }

        return result;
    }

    /**
     * 把高德 6 位类型码映射成中文大类名（提升语义检索与前端展示效果）
     */
    private String mapCategory(String typeCode) {
        if (typeCode == null || typeCode.length() < 2) {
            return typeCode;
        }
        switch (typeCode.substring(0, 2)) {
            case "05": return "餐饮服务";
            case "06": return "购物服务";
            case "07": return "生活服务";
            case "08": return "体育休闲";
            case "10": return "住宿服务";
            case "11": return "风景名胜";
            case "12": return "商务住宅";
            case "14": return "科教文化";
            case "15": return "交通设施";
            default: return typeCode;
        }
    }

    /**
     * 调试用：返回高德原始响应中的第一条 POI（含 photos/biz_ext/tag/business_area/opentime 等未建模字段）
     */
    public JsonNode fetchFirstPoiRaw() {
        String url = amapConfig.getBaseUrl() + "/v3/place/text"
                + "?key=" + amapConfig.getKey()
                + "&types=" + TYPES[0]
                + "&city=" + FOSHAN_ADCODE
                + "&citylimit=true"
                + "&offset=25&page=1&extensions=all";

        log.info("===== 调试请求 URL: {}", url);

        String raw = restTemplate.getForObject(url, String.class);
        if (raw == null) {
            log.warn("===== 高德原始响应为空");
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode pois = root.path("pois");
            if (!pois.isArray() || pois.isEmpty()) {
                log.warn("===== 高德返回无 POI 数据, raw={}", raw);
                return null;
            }
            return pois.get(0);
        } catch (Exception e) {
            log.error("===== 解析高德原始响应失败, raw={}", raw, e);
            return null;
        }
    }

    /**
     * 回填高德 POI ID：按 lng/lat + name 用 place/around 匹配（每次最多 20 条）
     */
    public int backfillAmapId(Long cityId) {
        List<Poi> pois = poiMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Poi>()
                        .eq(Poi::getCityId, cityId)
                        .isNull(Poi::getAmapId)
                        .orderByAsc(Poi::getId)
                        .last("limit 20")
        );

        System.out.println("backfill 查到 " + pois.size() + " 条, cityId=" + cityId);

        int updated = 0;
        for (Poi poi : pois) {
            try {
                if (poi.getLng() == null || poi.getLat() == null) {
                    log.warn("===== backfill 无坐标，跳过: name={}", poi.getName());
                    continue;
                }
                String location = poi.getLng() + "," + poi.getLat();
                String url = amapConfig.getBaseUrl() + "/v3/place/around"
                        + "?key=" + amapConfig.getKey()
                        + "&location=" + location
                        + "&radius=500"
                        + "&keywords=" + URLEncoder.encode(poi.getName(), StandardCharsets.UTF_8)
                        + "&offset=1&page=1&extensions=all";

                System.out.println("backfill URL: " + url);

                String raw = restTemplate.getForObject(url, String.class);
                System.out.println("backfill raw: " + raw);
                AmapPoiResponse response = objectMapper.readValue(raw, AmapPoiResponse.class);
                if (response == null || !"1".equals(response.getStatus())
                        || response.getPois() == null || response.getPois().isEmpty()) {
                    log.warn("===== backfill 未命中: name={}", poi.getName());
                    System.out.println("backfill 未命中详情: name=" + poi.getName()
                            + ", status=" + (response != null ? response.getStatus() : "null")
                            + ", pois=" + (response != null && response.getPois() != null ? response.getPois().size() : "null")
                            + ", info=" + (response != null ? response.getInfo() : "null"));
                    continue;
                }

                AmapPoiResponse.PoiItem item = response.getPois().get(0);

                Poi update = new Poi();
                update.setId(poi.getId());
                update.setAmapId(item.getId());
                if (item.getPhotos() != null && !item.getPhotos().isEmpty()) {
                    update.setPhotos(objectMapper.writeValueAsString(item.getPhotos()));
                }
                poiMapper.updateById(update);
                updated++;

                System.out.println("backfill name=" + poi.getName() + " amapId=" + item.getId());

                Thread.sleep(400);
            } catch (Exception e) {
                log.error("===== backfill 失败: name={}", poi.getName(), e);
            }
        }
        System.out.println("backfill 完成, updated=" + updated);
        return updated;
    }

    /**
     * 补数据：按高德 POI ID 用 place/detail 精确查询，回填 photos（每次最多 20 条）
     */
    public int refreshPhotos(Long cityId) {
        List<Poi> pois = poiMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Poi>()
                        .eq(Poi::getCityId, cityId)
                        .isNotNull(Poi::getAmapId)
                        .isNull(Poi::getPhotos)
                        .orderByAsc(Poi::getId)
                        .last("limit 20")
        );

        System.out.println("refreshPhotos 查到 " + pois.size() + " 条, cityId=" + cityId);

        int updated = 0;
        for (Poi poi : pois) {
            try {
                String url = amapConfig.getBaseUrl() + "/v3/place/detail"
                        + "?id=" + poi.getAmapId()
                        + "&key=" + amapConfig.getKey()
                        + "&extensions=all";

                AmapPoiResponse response = restTemplate.getForObject(url, AmapPoiResponse.class);
                if (response == null || !"1".equals(response.getStatus())
                        || response.getPois() == null || response.getPois().isEmpty()) {
                    log.warn("===== refresh-photos 未命中: name={}", poi.getName());
                    continue;
                }

                AmapPoiResponse.PoiItem item = response.getPois().get(0);

                if (item.getPhotos() != null && !item.getPhotos().isEmpty()) {
                    Poi update = new Poi();
                    update.setId(poi.getId());
                    update.setPhotos(objectMapper.writeValueAsString(item.getPhotos()));
                    poiMapper.updateById(update);
                    updated++;
                    log.info("===== refresh-photos 更新: name={}, photos={}",
                            poi.getName(), item.getPhotos().size());
                }

                Thread.sleep(400);
            } catch (Exception e) {
                log.error("===== refresh-photos 失败: name={}", poi.getName(), e);
            }
        }
        System.out.println("refreshPhotos 完成, updated=" + updated);
        return updated;
    }
}