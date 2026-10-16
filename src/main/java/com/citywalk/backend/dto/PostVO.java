package com.citywalk.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private Long poiId;
    private Long routeId;
    private String title;
    private String content;
    private List<String> images;
    private LocalDateTime createdAt;
    private String authorNickname;
    private String authorAvatar;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private String poiName;
}
