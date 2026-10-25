package com.devicecare360.technician.service;

import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {

    private final TechnicianProfileRepository repository;

    public TechnicianProfile registerTechnician(TechnicianProfile profile) {
        if (profile.getId() != null && repository.existsById(profile.getId())) {
            log.info("TechnicianProfile already exists for ID: {}. Returning existing record.", profile.getId());
            return repository.findById(profile.getId()).get();
        }
        profile.setVerificationStatus("PENDING");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        TechnicianProfile saved = repository.save(profile);
        log.info("TechnicianProfile successfully persisted for ID: {}", saved.getId());
        return saved;
    }

    public List<TechnicianProfile> getAllTechnicians() {
        return repository.findAll();
    }

    public TechnicianProfile getTechnicianById(String id) {
        return repository.findById(id)
                .orElseGet(() -> {
                    log.warn("TechnicianProfile not found for ID: {}. Initializing default profile.", id);
                    TechnicianProfile fallback = TechnicianProfile.builder()
                            .id(id)
                            .available(true)
                            .verificationStatus("PENDING")
                            .averageRating(0.0)
                            .totalReviews(0)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return repository.save(fallback);
                });
    }

    public TechnicianProfile updateTechnician(String id, TechnicianProfile updateRequest) {
        TechnicianProfile existing = getTechnicianById(id);
        if (updateRequest.getFullName() != null) existing.setFullName(updateRequest.getFullName());
        if (updateRequest.getPhone() != null) existing.setPhone(updateRequest.getPhone());
        if (updateRequest.getSpecializations() != null) existing.setSpecializations(updateRequest.getSpecializations());
        if (updateRequest.getServiceAreas() != null) existing.setServiceAreas(updateRequest.getServiceAreas());
        if (updateRequest.getVerificationStatus() != null) existing.setVerificationStatus(updateRequest.getVerificationStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public TechnicianProfile setAvailability(String id, boolean available) {
        TechnicianProfile existing = getTechnicianById(id);
        existing.setAvailable(available);
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public List<TechnicianProfile> searchTechnicians(String specialization, String serviceArea, Boolean available, String status) {
        if (specialization != null && !specialization.isBlank()) {
            return repository.findBySpecializationsContaining(specialization);
        } else if (serviceArea != null && !serviceArea.isBlank()) {
            return repository.findByServiceAreasContaining(serviceArea);
        } else if (status != null && !status.isBlank()) {
            return repository.findByVerificationStatus(status);
        } else if (available != null) {
            return repository.findByAvailable(available);
        }
        return repository.findAll();
    }
}
