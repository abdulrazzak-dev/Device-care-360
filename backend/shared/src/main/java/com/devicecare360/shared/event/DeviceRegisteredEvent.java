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
public class DeviceRegisteredEvent implements Serializable {
    private String deviceId;
    private String userId;
    private String category;
    private String brand;
    private String model;
    private String serialNumber;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
