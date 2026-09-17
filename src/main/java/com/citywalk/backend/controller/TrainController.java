package com.citywalk.backend.controller;

import com.citywalk.backend.dto.TrainQueryResponse;
import com.citywalk.backend.service.TrainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "票价", description = "12306 票价查询")
@RestController
@RequestMapping("/api/train")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @Operation(summary = "查询火车票价格")
    @GetMapping("/query")
    public TrainQueryResponse query(@RequestParam String from,
                                    @RequestParam String to,
                                    @RequestParam String date) {
        return trainService.query(from, to, date);
    }
}