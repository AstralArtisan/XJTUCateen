package com.xjtu.canteen.controller;

import com.xjtu.canteen.common.ApiResponse;
import com.xjtu.canteen.mapper.HealthMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    private final HealthMapper healthMapper;

    public HealthController(HealthMapper healthMapper) {
        this.healthMapper = healthMapper;
    }

    @GetMapping("/health")
    public ApiResponse health() {
        return ApiResponse.success(Map.of("db", healthMapper.ping() == 1 ? "ok" : "bad"));
    }
}
