package com.devicecare360.admin.repository;

import com.devicecare360.admin.document.AdminAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AdminAuditLogRepository extends MongoRepository<AdminAuditLog, String> {
    List<AdminAuditLog> findByAdminIdOrderByTimestampDesc(String adminId);
}
