package com.devicecare360.device.service;

import com.devicecare360.device.client.AiTroubleshootingClient;
import com.devicecare360.device.config.RabbitMQConfig;
import com.devicecare360.device.document.UserDevice;
import com.devicecare360.device.repository.UserDeviceRepository;
import com.devicecare360.shared.event.DeviceRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AiTroubleshootingClient aiTroubleshootingClient;

    private static final List<String> STANDARD_CATEGORIES = List.of(
            "Smartphone", "Laptop", "Television", "Refrigerator",
            "Washing Machine", "Air Conditioner", "Printer", "Computer",
            "Tablet", "Audio Device", "Other"
    );

    private static final Map<String, List<String>> BRAND_CATALOG = Map.of(
            "Smartphone", List.of("Apple", "Samsung", "Google", "Xiaomi", "OnePlus", "Other"),
            "Laptop", List.of("Dell", "HP", "Lenovo", "Apple", "Asus", "Acer", "Other"),
            "Television", List.of("Samsung", "LG", "Sony", "TCL", "Hisense", "Other"),
            "Refrigerator", List.of("Whirlpool", "Samsung", "LG", "Bosch", "Haier", "Other")
    );

    private static final Map<String, List<String>> COMMON_ISSUES = Map.of(
            "Smartphone", List.of("Screen Cracked", "Battery Draining Fast", "Overheating", "Charging Port Fault", "No Signal"),
            "Laptop", List.of("Blue Screen / Crash", "Keyboard Keys Not Working", "Overheating Fan Noise", "No Power", "Slow Performance"),
            "Television", List.of("No Display / Black Screen", "No Sound", "Flickering Display", "Remote Not Responding"),
            "Refrigerator", List.of("Not Cooling", "Water Leaking", "Unusual Noise", "Ice Maker Not Working")
    );

    public List<String> getCategories() {
        return STANDARD_CATEGORIES;
    }

    public List<String> getBrands(String category) {
        return BRAND_CATALOG.getOrDefault(category, List.of("Generic", "Custom", "Other"));
    }

    public List<String> getCommonIssues(String category) {
        return COMMON_ISSUES.getOrDefault(category, List.of("Power Issue", "Performance Issue", "Physical Damage", "Software Glitch"));
    }

    public String resolveCategory(String categoryInput) {
        if (STANDARD_CATEGORIES.contains(categoryInput)) {
            return categoryInput;
        }
        try {
            var response = aiTroubleshootingClient.normalizeCategory(Map.of("category", categoryInput));
            return (response != null && response.getData() != null) ? response.getData() : "Other";
        } catch (Exception e) {
            log.error("AI Category Normalization failed: {}", e.getMessage());
            return "Other";
        }
    }

    public String resolveBrand(String brandInput) {
        try {
            var response = aiTroubleshootingClient.normalizeBrand(Map.of("brand", brandInput));
            return (response != null && response.getData() != null) ? response.getData() : brandInput;
        } catch (Exception e) {
            log.error("AI Brand Normalization failed: {}", e.getMessage());
            return brandInput;
        }
    }

    public UserDevice createDevice(UserDevice device) {
        UserDevice saved = userDeviceRepository.save(device);
        DeviceRegisteredEvent event = DeviceRegisteredEvent.builder()
                .deviceId(saved.getId())
                .userId(saved.getUserId())
                .category(saved.getCategory())
                .brand(saved.getBrand())
                .model(saved.getModel())
                .serialNumber(saved.getSerialNumber())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.DEVICE_REGISTERED_ROUTING_KEY, event);
        return saved;
    }

    public List<UserDevice> getDevicesByUserId(String userId) {
        return userDeviceRepository.findByUserId(userId);
    }

    public UserDevice getDeviceById(String id) {
        return userDeviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + id));
    }

    public UserDevice updateDevice(String id, UserDevice updateRequest) {
        UserDevice existing = getDeviceById(id);
        if (updateRequest.getCategory() != null) existing.setCategory(updateRequest.getCategory());
        if (updateRequest.getBrand() != null) existing.setBrand(updateRequest.getBrand());
        if (updateRequest.getModel() != null) existing.setModel(updateRequest.getModel());
        if (updateRequest.getSerialNumber() != null) existing.setSerialNumber(updateRequest.getSerialNumber());
        if (updateRequest.getPurchaseDate() != null) existing.setPurchaseDate(updateRequest.getPurchaseDate());
        if (updateRequest.getWarrantyInfo() != null) existing.setWarrantyInfo(updateRequest.getWarrantyInfo());
        return userDeviceRepository.save(existing);
    }

    public void deleteDevice(String id) {
        userDeviceRepository.deleteById(id);
    }
}
