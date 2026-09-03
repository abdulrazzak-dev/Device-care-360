package com.devicecare360.aitroubleshooting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleshootingRequest {
    @NotBlank
    private String category;
    private String brand;
    private String model;
    @NotBlank
    private String issueDescription;
}
