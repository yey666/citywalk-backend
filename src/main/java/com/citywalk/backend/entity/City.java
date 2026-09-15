package com.citywalk.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("city")
public class City {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String region;

    private BigDecimal lat;

    private BigDecimal lng;

    private String description;

    private String coverImage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}