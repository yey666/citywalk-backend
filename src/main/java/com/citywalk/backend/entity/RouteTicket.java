package com.citywalk.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route_ticket")
public class RouteTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long routeId;
    private String trainCode;
    private String fromStation;
    private String toStation;
    private String startTime;
    private String arriveTime;
    private String travelDate;
    private String pricesJson;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
