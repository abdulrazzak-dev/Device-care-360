package com.devicecare360.admin.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "admin_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLog {
    @Id
    private String id;
    private String adminId;
    private String action;
    private String targetEntity;
    private String targetId;
    private String details;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
