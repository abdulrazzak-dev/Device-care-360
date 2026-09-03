package com.devicecare360.device.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "user_devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {
    @Id
    private String id;
    private String userId;
    private String category;
    private String brand;
    private String model;
    private String serialNumber;
    private LocalDate purchaseDate;
    private String warrantyInfo;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
