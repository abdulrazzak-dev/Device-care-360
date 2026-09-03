package com.devicecare360.aitroubleshooting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleshootingResponse {
    private String problemSummary;
    private List<String> possibleCauses;
    private String riskLevel; // LOW | MEDIUM | HIGH | CRITICAL
    private List<String> safeTroubleshootingSteps;
    private List<String> doNotAttempt;
    private List<String> safetyWarnings;
    private String recommendedAction;
    private boolean requiresProfessional;
    private String recommendedTechnicianType;
    private List<String> maintenanceTips;
}
