package com.citywalk.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmapPoiResponse {

    private String status;

    private String info;

    private String count;

    private List<PoiItem> pois;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PoiItem {

        private String id;

        private String name;

        private String type;

        private String address;

        private String location;

        @JsonProperty("pname")
        private String province;

        @JsonProperty("cityname")
        private String city;

        @JsonProperty("adname")
        private String district;

        @JsonDeserialize(using = PhotosDeserializer.class)
        private List<Photo> photos;

        @JsonProperty("biz_ext")
        @JsonDeserialize(using = BizExtDeserializer.class)
        private BizExt bizExt;

        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String opentime2;

        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String tag;

        @JsonProperty("business_area")
        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String businessArea;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Photo {
        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String url;

        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String title;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BizExt {
        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String rating;

        private Object cost;   // 高德返回数组，如 ["30","50"]
    }

    /** "" → null；数组 → List<Photo> */
    public static class PhotosDeserializer extends JsonDeserializer<List<Photo>> {
        @Override
        public List<Photo> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                return null;
            }
            if (p.currentToken() != JsonToken.START_ARRAY) {
                return null;
            }
            List<Photo> photos = new ArrayList<>();
            JsonNode node = p.getCodec().readTree(p);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    photos.add(p.getCodec().treeToValue(item, Photo.class));
                }
            }
            return photos;
        }
    }

    /** "" → null；对象 → BizExt */
    public static class BizExtDeserializer extends JsonDeserializer<BizExt> {
        @Override
        public BizExt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                return null;
            }
            return p.readValueAs(BizExt.class);
        }
    }

    /** "" → null；数组 → 取第一个元素；字符串 → 原样 */
    public static class StringOrArrayDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.currentToken();
            if (token == JsonToken.VALUE_STRING) {
                String s = p.getValueAsString();
                return (s == null || s.isEmpty()) ? null : s;
            }
            if (token == JsonToken.START_ARRAY) {
                JsonNode node = p.getCodec().readTree(p);
                if (node.isArray() && node.size() > 0) {
                    JsonNode first = node.get(0);
                    return first.isTextual() ? first.asText() : first.toString();
                }
                return null;
            }
            return null;
        }
    }
}
