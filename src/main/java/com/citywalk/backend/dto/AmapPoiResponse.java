package com.citywalk.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
    }
}