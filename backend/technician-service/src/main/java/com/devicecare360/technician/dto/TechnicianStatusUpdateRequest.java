package com.devicecare360.technician.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianStatusUpdateRequest {
    @NotBlank(message = "Verification status is required")
    private String status;

    @JsonAlias({"reason", "rejectionReason"})
    private String rejectionReason;
}
