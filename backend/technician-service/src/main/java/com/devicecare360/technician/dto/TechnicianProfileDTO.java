package com.devicecare360.technician.dto;

import com.devicecare360.technician.document.TechnicianProfile;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianProfileDTO {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private List<String> specializations;
    private List<String> categories;
    private List<String> serviceAreas;
    private String city;
    private String serviceArea;
    private String professionalBio;

    @JsonProperty("isAvailable")
    private Boolean isAvailable;

    private String verificationStatus;
    private Double averageRating;
    private Integer totalReviews;
    private Double hourlyRate;
    private Integer experienceYears;
    private String profileImageUrl;

    public static TechnicianProfileDTO fromEntity(TechnicianProfile entity) {
        if (entity == null) return null;
        List<String> specs = entity.getSpecializations() != null ? entity.getSpecializations() : Collections.emptyList();
        return TechnicianProfileDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .specializations(specs)
                .categories(specs)
                .serviceAreas(entity.getServiceAreas() != null ? entity.getServiceAreas() : Collections.emptyList())
                .city(entity.getCity())
                .serviceArea(entity.getServiceArea())
                .professionalBio(entity.getProfessionalBio())
                .isAvailable(entity.getIsAvailable())
                .verificationStatus(entity.getVerificationStatus() != null ? entity.getVerificationStatus() : "PENDING")
                .averageRating(entity.getAverageRating())
                .totalReviews(entity.getTotalReviews())
                .hourlyRate(entity.getHourlyRate() != null ? entity.getHourlyRate() : 2500.0)
                .experienceYears(entity.getExperienceYears())
                .profileImageUrl(entity.getProfileImageUrl())
                .build();
    }
}
