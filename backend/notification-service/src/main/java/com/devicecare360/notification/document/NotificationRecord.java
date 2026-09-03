package com.devicecare360.notification.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecord {
    @Id
    private String id;
    private String userId;
    private String title;
    private String message;
    private String channel; // IN_APP, EMAIL, SMS
    private String type; // BOOKING, PAYMENT, SAFETY_ALERT
    @Builder.Default
    private boolean read = false;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
