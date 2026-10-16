package com.citywalk.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private Long poiId;
    private Long routeId;
    private String title;
    private String content;
    private List<String> images;
}
