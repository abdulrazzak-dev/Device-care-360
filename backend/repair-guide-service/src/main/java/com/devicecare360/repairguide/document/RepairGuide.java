package com.devicecare360.repairguide.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "repair_guides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairGuide {
    @Id
    private String id;
    private String title;
    private String category;
    private String brand;
    private String issue;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String skillLevel; // BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL_ONLY
    private List<String> toolsRequired;
    private List<String> safeInstructions;
    private List<String> safetyWarnings;
    private String officialDocumentationUrl;
    private List<String> maintenanceTips;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
