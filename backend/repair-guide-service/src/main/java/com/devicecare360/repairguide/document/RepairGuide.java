package com.devicecare360.repairguide.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("approved")
    private Boolean approved = true;

    @JsonProperty("estimatedTime")
    private String estimatedTime;

    // Aliases and convenience accessors for modern UI and diverse client representations
    @JsonProperty("difficulty")
    @JsonAlias({"skillLevel", "difficulty"})
    private String difficulty;

    @JsonProperty("description")
    @JsonAlias({"issue", "description"})
    private String description;

    @JsonProperty("steps")
    @JsonAlias({"safeInstructions", "steps"})
    private List<String> steps;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getDifficulty() {
        return difficulty != null ? difficulty : skillLevel;
    }

    public String getSkillLevel() {
        return skillLevel != null ? skillLevel : difficulty;
    }

    public String getDescription() {
        return description != null ? description : issue;
    }

    public String getIssue() {
        return issue != null ? issue : description;
    }

    public List<String> getSteps() {
        return steps != null ? steps : safeInstructions;
    }

    public List<String> getSafeInstructions() {
        return safeInstructions != null ? safeInstructions : steps;
    }

    public Boolean getApproved() {
        return approved != null ? approved : true;
    }
}
