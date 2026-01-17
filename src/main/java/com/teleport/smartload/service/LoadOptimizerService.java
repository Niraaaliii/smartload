package com.teleport.smartload.service;

import com.teleport.smartload.dto.OptimizeRequest;
import com.teleport.smartload.dto.OptimizeResponse;

public interface LoadOptimizerService {

    OptimizeResponse optimize(OptimizeRequest request);
}
