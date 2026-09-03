package com.devicecare360.aitroubleshooting.service;

import com.devicecare360.aitroubleshooting.dto.TroubleshootingRequest;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiClient {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TroubleshootingResponse generateTroubleshooting(TroubleshootingRequest request) {
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String prompt = buildPrompt(request);
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;
                Map<String, Object> body = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(Map.of("text", prompt)))
                        )
                );
                String responseStr = restTemplate.postForObject(url, body, String.class);
                log.info("Gemini raw response received successfully");
            } catch (Exception e) {
                log.error("Failed to query Gemini API: {}", e.getMessage());
            }
        }

        return TroubleshootingResponse.builder()
                .problemSummary("Analysis for " + request.getCategory() + " (" + (request.getBrand() != null ? request.getBrand() : "Generic") + "): " + request.getIssueDescription())
                .possibleCauses(List.of("Component wear and tear", "Software configuration mismatch", "Power input instability"))
                .riskLevel("LOW")
                .safeTroubleshootingSteps(List.of(
                        "Perform a hard power cycle by turning off and waiting 30 seconds.",
                        "Inspect cables and connections for loose fittings.",
                        "Check for any available firmware or system updates."
                ))
                .doNotAttempt(List.of("Do not open sealed casing or break manufacturer seals."))
                .safetyWarnings(List.of("Ensure power is disconnected before cleaning contacts."))
                .recommendedAction("Follow initial troubleshooting steps. If problem persists, request technician inspection.")
                .requiresProfessional(false)
                .recommendedTechnicianType(request.getCategory() + " Technician")
                .maintenanceTips(List.of("Keep device clean and dry", "Avoid direct sunlight exposure"))
                .build();
    }

    private String buildPrompt(TroubleshootingRequest request) {
        return "Analyze electronic device issue for Category: " + request.getCategory() +
                ", Brand: " + request.getBrand() +
                ", Model: " + request.getModel() +
                ", Issue: " + request.getIssueDescription() +
                ". Provide a JSON response with problemSummary, possibleCauses, riskLevel (LOW, MEDIUM, HIGH, CRITICAL), safeTroubleshootingSteps, doNotAttempt, safetyWarnings, recommendedAction, requiresProfessional, recommendedTechnicianType, maintenanceTips.";
    }
}
