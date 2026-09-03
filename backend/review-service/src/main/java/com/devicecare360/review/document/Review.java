package com.devicecare360.review.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    private String id;
    private String bookingId;
    private String userId;
    private String technicianId;
    private int rating; // 1 to 5
    private String comment;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
