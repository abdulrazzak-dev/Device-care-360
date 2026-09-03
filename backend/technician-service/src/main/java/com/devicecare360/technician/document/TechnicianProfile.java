package com.devicecare360.technician.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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
    private List<String> specializations; // Smartphone, Laptop, Television, Refrigerator, etc.
    private List<String> serviceAreas; // City/Zip codes
    @Builder.Default
    private boolean available = true;
    @Builder.Default
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED
    @Builder.Default
    private double averageRating = 0.0;
    @Builder.Default
    private int totalReviews = 0;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
