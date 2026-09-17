package com.citywalk.backend.controller;

import com.citywalk.backend.entity.City;
import com.citywalk.backend.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "城市", description = "城市列表、详情")
@RestController
@RequestMapping("/api/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @Operation(summary = "获取城市列表")
    @GetMapping("/list")
    public List<City> list() {
        return cityService.listAll();
    }

    @Operation(summary = "获取城市详情")
    @GetMapping("/{id}/overview")
    public City overview(@PathVariable Long id) {
        return cityService.getById(id);
    }
}