package com.devicecare360.booking.client;

import com.devicecare360.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "technician-service", path = "/api/technicians")
public interface TechnicianClient {

    @GetMapping("/{id}")
    ApiResponse<Map<String, Object>> getTechnicianById(@PathVariable("id") String id);
}
