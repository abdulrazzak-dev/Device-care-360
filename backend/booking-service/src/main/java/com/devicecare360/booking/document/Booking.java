package com.devicecare360.booking.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String id;
    private String userId;
    private String technicianId;
    private String technicianName;
    private String deviceId;
    private String deviceModel;
    private String deviceCategory;
    private String issueDescription;

    @JsonAlias({"appointmentDate", "appointment_date"})
    private LocalDateTime appointmentTime;

    private BigDecimal estimatedCost;
    @Builder.Default
    private String status = "PENDING"; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    private String cancellationReason;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
