package com.devicecare360.admin.client;

import com.devicecare360.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "technician-service", path = "/api/technicians")
public interface TechnicianAdminClient {

    @PutMapping("/{id}")
    ApiResponse<Map<String, Object>> updateTechnician(@PathVariable("id") String id, @RequestBody Map<String, Object> body);
}
