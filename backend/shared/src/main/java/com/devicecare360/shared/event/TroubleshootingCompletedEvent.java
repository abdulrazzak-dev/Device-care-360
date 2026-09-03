package com.devicecare360.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleshootingCompletedEvent implements Serializable {
    private String troubleshootingId;
    private String userId;
    private String category;
    private String brand;
    private String issueDescription;
    private String riskLevel;
    private boolean requiresProfessional;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
