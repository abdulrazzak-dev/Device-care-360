package com.devicecare360.device.client;

import com.devicecare360.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ai-troubleshooting-service", path = "/api/troubleshooting")
public interface AiTroubleshootingClient {

    @PostMapping("/category-search")
    ApiResponse<String> normalizeCategory(@RequestBody Map<String, String> request);

    @PostMapping("/brand-search")
    ApiResponse<String> normalizeBrand(@RequestBody Map<String, String> request);
}
