package com.citywalk.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citywalk.backend.entity.Route;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RouteMapper extends BaseMapper<Route> {
}