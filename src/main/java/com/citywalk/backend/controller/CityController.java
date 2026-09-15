package com.citywalk.backend.controller;

import com.citywalk.backend.entity.City;
import com.citywalk.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/list")
    public List<City> list() {
        return cityService.listAll();
    }
}