package com.citywalk.backend.service;

import com.citywalk.backend.entity.City;

import java.util.List;

public interface CityService {

    List<City> listAll();
    City getById(Long id);
}