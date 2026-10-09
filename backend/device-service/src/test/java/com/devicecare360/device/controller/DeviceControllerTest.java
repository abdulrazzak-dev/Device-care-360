package com.devicecare360.device.controller;

import com.devicecare360.device.document.UserDevice;
import com.devicecare360.device.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @Test
    @DisplayName("GET /api/devices/categories returns list of categories")
    void testGetCategories() throws Exception {
        Mockito.when(deviceService.getCategories())
                .thenReturn(List.of("Smartphone", "Laptop", "Television"));

        mockMvc.perform(get("/api/devices/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0]", is("Smartphone")));
    }

    @Test
    @DisplayName("GET /api/devices/brands?category=Smartphone returns brands for category")
    void testGetBrands() throws Exception {
        Mockito.when(deviceService.getBrands("Smartphone"))
                .thenReturn(List.of("Apple", "Samsung", "Google"));

        mockMvc.perform(get("/api/devices/brands").param("category", "Smartphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0]", is("Apple")));
    }

    @Test
    @DisplayName("GET /api/devices/issues?category=Smartphone returns common issues")
    void testGetCommonIssues() throws Exception {
        Mockito.when(deviceService.getCommonIssues("Smartphone"))
                .thenReturn(List.of("Battery draining fast", "Cracked screen"));

        mockMvc.perform(get("/api/devices/issues").param("category", "Smartphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0]", is("Battery draining fast")));
    }

    @Test
    @DisplayName("POST /api/devices creates device with X-User-Id header")
    void testCreateDevice() throws Exception {
        UserDevice inputDevice = UserDevice.builder()
                .brand("Apple")
                .model("iPhone 14")
                .category("Smartphone")
                .build();

        UserDevice createdDevice = UserDevice.builder()
                .id("device-1")
                .userId("user-123")
                .brand("Apple")
                .model("iPhone 14")
                .category("Smartphone")
                .build();

        Mockito.when(deviceService.createDevice(any(UserDevice.class)))
                .thenReturn(createdDevice);

        mockMvc.perform(post("/api/devices")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDevice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("device-1")))
                .andExpect(jsonPath("$.data.userId", is("user-123")));
    }
}
