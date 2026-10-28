package com.devicecare360.technician.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianUpdateRequest {
    private String fullName;
    private String phone;

    @JsonAlias({"categories"})
    private List<String> specializations;

    private String city;
    private String serviceArea;
    private List<String> serviceAreas;

    @Min(value = 0, message = "Experience years must be between 0 and 50")
    @Max(value = 50, message = "Experience years must be between 0 and 50")
    @JsonAlias({"yearsOfExperience"})
    private Integer experienceYears;

    @PositiveOrZero(message = "Hourly rate must be non-negative")
    private Double hourlyRate;

    @JsonAlias({"available"})
    private Boolean isAvailable;

    private String professionalBio;
    private String profileImageUrl;
}
