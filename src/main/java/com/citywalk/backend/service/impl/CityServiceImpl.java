package com.citywalk.backend.service.impl;

import com.citywalk.backend.entity.City;
import com.citywalk.backend.mapper.CityMapper;
import com.citywalk.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityMapper cityMapper;

    @Override
    public List<City> listAll() {
        return cityMapper.selectList(null);
    }
    @Override
    public City getById(Long id) {
        return cityMapper.selectById(id);
    }
}