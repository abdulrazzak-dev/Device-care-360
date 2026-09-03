package com.devicecare360.notification.service;

import com.devicecare360.notification.document.NotificationRecord;
import com.devicecare360.notification.repository.NotificationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRecordRepository repository;

    public List<NotificationRecord> getUserNotifications(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public NotificationRecord markAsRead(String id) {
        NotificationRecord record = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found for ID: " + id));
        record.setRead(true);
        return repository.save(record);
    }
}
