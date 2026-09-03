package com.devicecare360.aitroubleshooting.document;

import com.devicecare360.aitroubleshooting.dto.TroubleshootingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "troubleshooting_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleshootingLog {
    @Id
    private String id;
    private String userId;
    private String category;
    private String brand;
    private String model;
    private String issueDescription;
    private TroubleshootingResponse response;
    private boolean highRiskDetected;
    private String hazardCategory;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
