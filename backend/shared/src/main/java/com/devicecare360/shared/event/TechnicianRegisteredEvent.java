package com.devicecare360.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianRegisteredEvent implements Serializable {
    private String technicianId;
    private String userId;
    private String email;
    private String fullName;
    private String phone;
    private List<String> specializations;
    private List<String> serviceAreas;
    private Integer yearsOfExperience;
    private Double hourlyRate;
    private String city;
    private String serviceArea;
    private String professionalBio;
    private String certificateUrl;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
