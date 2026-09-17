package com.citywalk.backend.service;

import com.citywalk.backend.dto.TrainQueryResponse;

public interface TrainService {

    TrainQueryResponse query(String from, String to, String date);
}