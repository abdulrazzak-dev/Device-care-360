package com.devicecare360.aitroubleshooting.repository;

import com.devicecare360.aitroubleshooting.document.TroubleshootingLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TroubleshootingLogRepository extends MongoRepository<TroubleshootingLog, String> {
    List<TroubleshootingLog> findByUserIdOrderByTimestampDesc(String userId);
}
