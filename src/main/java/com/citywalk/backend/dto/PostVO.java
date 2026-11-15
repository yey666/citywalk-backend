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
    private String days;   // 行程天数，如“3天2晚”
    private String tag;    // 卡片标签，如“跨城大环线”
    private Boolean collected;   // 当前用户是否收藏
    private Integer collectCount; // 收藏数
}
