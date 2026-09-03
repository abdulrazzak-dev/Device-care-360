package com.devicecare360.notification.repository;

import com.devicecare360.notification.document.NotificationRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRecordRepository extends MongoRepository<NotificationRecord, String> {
    List<NotificationRecord> findByUserIdOrderByCreatedAtDesc(String userId);
}
