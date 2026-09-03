package com.devicecare360.review.client;

import com.devicecare360.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "booking-service", path = "/api/bookings")
public interface BookingClient {

    @GetMapping("/{id}")
    ApiResponse<Map<String, Object>> getBookingById(@PathVariable("id") String id);
}
