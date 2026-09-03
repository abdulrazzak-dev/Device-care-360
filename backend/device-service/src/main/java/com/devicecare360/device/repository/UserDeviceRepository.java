package com.devicecare360.device.repository;

import com.devicecare360.device.document.UserDevice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserDeviceRepository extends MongoRepository<UserDevice, String> {
    List<UserDevice> findByUserId(String userId);
}
