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
public class ReviewCreatedEvent implements Serializable {
    private String reviewId;
    private String bookingId;
    private String userId;
    private String technicianId;
    private int rating; // 1 to 5
    private String comment;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
