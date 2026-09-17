package com.citywalk.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("route_node")
public class RouteNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long routeId;
    private Long poiId;
    private Integer sortOrder;
    private Integer stayDuration;
    private String tip;
    private String photoSpot;
}