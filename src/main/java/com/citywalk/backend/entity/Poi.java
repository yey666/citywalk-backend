package com.citywalk.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("poi")
public class Poi {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cityId;

    private String name;

    private String category;

    private String address;

    private BigDecimal lat;

    private BigDecimal lng;

    private Integer photoScore;

    private Integer foodScore;

    private Integer historyScore;

    private Integer cultureScore;

    private String openHours;

    private Integer priceLevel;

    private String photoSpot;

    private String description;

    private LocalDateTime createdAt;

    private String avoidTip;

    private String restaurant;

    private String bestVisitTime;

    private String photos;
}