package com.citywalk.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route")
public class Route {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cityId;
    private String title;
    private String theme;
    private Integer duration;
    private String difficulty;
    private String bestTime;
    private String status;
    private String coverImage;
    private String description;
    private Integer isOfficial;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer dayIndex;
    private Long tripId;
    private Integer aiGenerated;
    private Integer viewCount;
    private Integer collectCount;
}