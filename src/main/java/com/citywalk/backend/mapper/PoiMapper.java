package com.citywalk.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citywalk.backend.entity.Poi;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PoiMapper extends BaseMapper<Poi> {
}