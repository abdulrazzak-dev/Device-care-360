package com.devicecare360.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent implements Serializable {
    private String bookingId;
    private String userId;
    private String technicianId;
    private String deviceId;
    private String issueDescription;
    private LocalDateTime appointmentTime;
    private BigDecimal estimatedCost;
    private String status;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
