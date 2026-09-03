package com.devicecare360.aitroubleshooting.safety;

import com.devicecare360.aitroubleshooting.dto.TroubleshootingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class SafetyEngine {

    private static final List<String> ELECTRICAL_KEYWORDS = List.of(
            "high voltage", "electrical shock", "exposed wire", "exposed wiring",
            "smoke", "sparks", "sparking", "burning smell", "capacitor", "internal mains"
    );

    private static final List<String> BATTERY_KEYWORDS = List.of(
            "battery swelling", "swollen battery", "expanding battery", "battery fire",
            "battery overheating", "thermal runaway", "lithium swelling", "lithium fire"
    );

    private static final List<String> GAS_PRESSURE_KEYWORDS = List.of(
            "gas line", "refrigerant", "freon", "compressor", "high pressure"
    );

    public TroubleshootingResult evaluateSafety(String category, String issueDescription, TroubleshootingResponse initialResponse) {
        String combinedText = (category + " " + issueDescription + " " +
                (initialResponse != null ? initialResponse.getProblemSummary() + " " + initialResponse.getPossibleCauses() : "")
        ).toLowerCase(Locale.ROOT);

        String hazardCategory = null;

        if (BATTERY_KEYWORDS.stream().anyMatch(combinedText::contains)) {
            hazardCategory = "BATTERY_THERMAL";
        } else if (ELECTRICAL_KEYWORDS.stream().anyMatch(combinedText::contains)) {
            hazardCategory = "ELECTRICAL";
        } else if (GAS_PRESSURE_KEYWORDS.stream().anyMatch(combinedText::contains)) {
            hazardCategory = "GAS_PRESSURE";
        }

        if (hazardCategory != null) {
            log.warn("SAFETY HAZARD DETECTED: {}", hazardCategory);

            List<String> warnings = new ArrayList<>();
            warnings.add("DANGER: Severe physical risk detected (" + hazardCategory + ").");
            warnings.add("Risk of fire, electrical shock, chemical exposure, or explosion.");

            List<String> doNotAttempt = new ArrayList<>();
            doNotAttempt.add("Do NOT open device casing or tamper with internal components.");
            doNotAttempt.add("Do NOT attempt to charge or pierce a swollen battery.");
            doNotAttempt.add("Do NOT touch exposed wiring or wet electrical elements.");

            List<String> safeSteps = new ArrayList<>();
            safeSteps.add("Unplug the device from wall outlet immediately if safe to reach.");
            safeSteps.add("Move device away from flammable materials.");
            safeSteps.add("Schedule an inspection with a certified professional technician.");

            TroubleshootingResponse enforcedResponse = TroubleshootingResponse.builder()
                    .problemSummary("CRITICAL SAFETY HAZARD DETECTED - " + hazardCategory)
                    .possibleCauses(List.of("Severe component degradation", "Internal short circuit", "Thermal or electrical failure"))
                    .riskLevel("CRITICAL")
                    .safeTroubleshootingSteps(safeSteps)
                    .doNotAttempt(doNotAttempt)
                    .safetyWarnings(warnings)
                    .recommendedAction("IMMEDIATELY CEASE USE & BOOK PROFESSIONAL TECHNICIAN")
                    .requiresProfessional(true)
                    .recommendedTechnicianType(category + " Specialist")
                    .maintenanceTips(List.of("Never operate devices exhibiting smoke, sparks, or swollen batteries."))
                    .build();

            return new TroubleshootingResult(enforcedResponse, true, hazardCategory);
        }

        return new TroubleshootingResult(initialResponse, false, null);
    }

    public record TroubleshootingResult(TroubleshootingResponse response, boolean highRisk, String hazardCategory) {}
}
