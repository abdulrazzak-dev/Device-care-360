package com.devicecare360.admin.service;

import com.devicecare360.admin.client.TechnicianAdminClient;
import com.devicecare360.admin.document.AdminAuditLog;
import com.devicecare360.admin.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminAuditLogRepository auditLogRepository;
    private final TechnicianAdminClient technicianAdminClient;

    public void verifyTechnician(String adminId, String technicianId, String status) {
        verifyTechnician(adminId, technicianId, status, null);
    }

    public void verifyTechnician(String adminId, String technicianId, String status, String reason) {
        Map<String, Object> body = new HashMap<>();
        body.put("verificationStatus", status);
        if (reason != null && !reason.isBlank()) {
            body.put("rejectionReason", reason.trim());
        }

        technicianAdminClient.updateTechnician(technicianId, body);

        AdminAuditLog auditLog = AdminAuditLog.builder()
                .adminId(adminId)
                .action("VERIFY_TECHNICIAN")
                .targetEntity("TECHNICIAN")
                .targetId(technicianId)
                .details("Status set to: " + status + (reason != null ? " | Reason: " + reason : ""))
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Admin {} verified technician {} with status {} (reason: {})", adminId, technicianId, status, reason);
    }

    public Map<String, Object> getDashboardStats() {
        return Map.of(
                "systemStatus", "HEALTHY",
                "activeServices", 11,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
