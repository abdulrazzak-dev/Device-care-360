package com.devicecare360.technician.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "technician_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianProfile {
    @Id
    private String id; // Matches auth userId
    private String fullName;
    private String email;
    private String phone;

    // Specializations / Categories
    @Builder.Default
    @JsonAlias({"categories"})
    private List<String> specializations = new ArrayList<>();

    @Builder.Default
    private List<String> serviceAreas = new ArrayList<>();

    private String city;
    private String serviceArea;
    private String professionalBio;

    // Availability
    @Builder.Default
    @JsonProperty("isAvailable")
    @JsonAlias({"available", "isAvailable"})
    private Boolean available = true;

    // Verification
    @Builder.Default
    private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED, SUSPENDED (VERIFIED is accepted as APPROVED)

    private String rejectionReason;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    // Credentials / Document upload
    private String certificateUrl;
    private String certificateFilename;
    private LocalDateTime certificateUploadedAt;

    // Professional metrics
    @Builder.Default
    private double averageRating = 0.0;

    @Builder.Default
    private int totalReviews = 0;

    @Builder.Default
    private Double hourlyRate = 2500.0;

    @Builder.Default
    @JsonAlias({"experienceYears"})
    private Integer yearsOfExperience = 5;

    private String profileImageUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Helper compatibility getters and setters
    public List<String> getCategories() {
        return specializations != null ? specializations : Collections.emptyList();
    }

    public void setCategories(List<String> categories) {
        if (categories != null) {
            this.specializations = categories;
        }
    }

    public Integer getExperienceYears() {
        return yearsOfExperience != null ? yearsOfExperience : 0;
    }

    public void setExperienceYears(Integer exp) {
        this.yearsOfExperience = exp;
    }

    public Boolean getIsAvailable() {
        return available != null ? available : false;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available);
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.available = isAvailable != null ? isAvailable : false;
    }

    public boolean isApproved() {
        if (verificationStatus == null) return false;
        String s = verificationStatus.trim().toUpperCase();
        return "APPROVED".equals(s) || "VERIFIED".equals(s);
    }
}
