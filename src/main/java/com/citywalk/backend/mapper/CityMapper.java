package com.citywalk.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citywalk.backend.entity.City;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CityMapper extends BaseMapper<City> {
}