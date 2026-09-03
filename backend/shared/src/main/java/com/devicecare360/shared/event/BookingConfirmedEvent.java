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
public class BookingConfirmedEvent implements Serializable {
    private String bookingId;
    private String userId;
    private String technicianId;
    private LocalDateTime appointmentTime;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
