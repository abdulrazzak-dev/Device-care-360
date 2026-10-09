package com.devicecare360.aitroubleshooting.controller;

import com.devicecare360.aitroubleshooting.document.TroubleshootingLog;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingRequest;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingResponse;
import com.devicecare360.aitroubleshooting.service.AiTroubleshootingService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiTroubleshootingController.class)
class AiTroubleshootingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiTroubleshootingService troubleshootingService;

    @Test
    @DisplayName("POST /api/troubleshooting/analyze with valid request returns 200 and diagnostic report")
    void testAnalyzeEndpointSuccess() throws Exception {
        TroubleshootingRequest request = TroubleshootingRequest.builder()
                .category("Smartphone")
                .brand("Apple")
                .model("iPhone 13")
                .issueDescription("Battery draining fast")
                .build();

        TroubleshootingResponse mockResponse = TroubleshootingResponse.builder()
                .problemSummary("Battery health degradation")
                .riskLevel("LOW")
                .requiresProfessional(false)
                .recommendedAction("Check battery health in settings")
                .possibleCauses(List.of("Background app refresh", "Aged battery cell"))
                .safeTroubleshootingSteps(List.of("Restart device", "Turn on Low Power Mode"))
                .build();

        Mockito.when(troubleshootingService.analyze(eq("user-123"), any(TroubleshootingRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/troubleshooting/analyze")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.riskLevel", is("LOW")))
                .andExpect(jsonPath("$.data.problemSummary", is("Battery health degradation")));
    }

    @Test
    @DisplayName("POST /api/troubleshooting/analyze without X-User-Id header defaults to ANONYMOUS user")
    void testAnalyzeEndpointAnonymousSuccess() throws Exception {
        TroubleshootingRequest request = TroubleshootingRequest.builder()
                .category("Laptop")
                .brand("Dell")
                .model("XPS 15")
                .issueDescription("Overheating while gaming")
                .build();

        TroubleshootingResponse mockResponse = TroubleshootingResponse.builder()
                .problemSummary("Thermal throttling")
                .riskLevel("MEDIUM")
                .requiresProfessional(false)
                .build();

        Mockito.when(troubleshootingService.analyze(eq("ANONYMOUS"), any(TroubleshootingRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/troubleshooting/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.riskLevel", is("MEDIUM")));
    }

    @Test
    @DisplayName("POST /api/troubleshooting/analyze with blank category returns 400 Bad Request")
    void testAnalyzeEndpointValidationFailure() throws Exception {
        TroubleshootingRequest request = TroubleshootingRequest.builder()
                .category("") // Blank category triggers @NotBlank
                .brand("Apple")
                .model("iPhone 13")
                .issueDescription("Will not turn on")
                .build();

        mockMvc.perform(post("/api/troubleshooting/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/troubleshooting/history/{userId} returns user history logs")
    void testGetHistory() throws Exception {
        TroubleshootingLog log = TroubleshootingLog.builder()
                .id("log-1")
                .userId("user-123")
                .category("Smartphone")
                .brand("Samsung")
                .issueDescription("Cracked screen")
                .build();

        Mockito.when(troubleshootingService.getHistory("user-123"))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/troubleshooting/history/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].category", is("Smartphone")));
    }
}
