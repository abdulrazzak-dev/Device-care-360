package com.devicecare360.technician.service;

import com.devicecare360.shared.model.DeviceSpecialization;
import com.devicecare360.shared.model.VerificationStatus;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.dto.CertificateUploadResponse;
import com.devicecare360.technician.dto.TechnicianProfileDTO;
import com.devicecare360.technician.dto.TechnicianUpdateRequest;
import com.devicecare360.technician.repository.TechnicianProfileRepository;
import com.devicecare360.technician.storage.CertificateValidator;
import com.devicecare360.technician.storage.DocumentStorageService;
import com.devicecare360.technician.storage.StoredDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {

    private final TechnicianProfileRepository repository;
    private final DocumentStorageService documentStorageService;
    private final CertificateValidator certificateValidator;

    private static final List<String> APPROVED_STATUSES = List.of("APPROVED", "VERIFIED");

    public TechnicianProfile registerTechnician(TechnicianProfile profile) {
        if (profile.getId() != null && repository.existsById(profile.getId())) {
            log.info("TechnicianProfile already exists for ID: {}. Returning existing record.", profile.getId());
            return repository.findById(profile.getId()).get();
        }

        // New profiles always default to PENDING regardless of client-supplied status
        profile.setVerificationStatus(VerificationStatus.PENDING.name());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        // Canonicalize specializations
        if (profile.getSpecializations() != null && !profile.getSpecializations().isEmpty()) {
            List<String> canonical = profile.getSpecializations().stream()
                    .map(DeviceSpecialization::normalizeToCanonical)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            profile.setSpecializations(canonical);
        }

        // Safe defaults
        if (profile.getAvailable() == null) profile.setAvailable(true);
        if (profile.getHourlyRate() == null) profile.setHourlyRate(2500.0);
        if (profile.getYearsOfExperience() == null) profile.setYearsOfExperience(1);

        TechnicianProfile saved = repository.save(profile);
        log.info("TechnicianProfile successfully persisted for ID: {}", saved.getId());
        return saved;
    }

    /**
     * Internal lookup by ID (e.g. called by BookingService Feign client or admin).
     */
    public TechnicianProfile getTechnicianById(String id) {
        return repository.findById(id)
                .orElseGet(() -> {
                    log.warn("TechnicianProfile not found for ID: {}. Initializing default profile.", id);
                    TechnicianProfile fallback = TechnicianProfile.builder()
                            .id(id)
                            .available(true)
                            .verificationStatus(VerificationStatus.PENDING.name())
                            .averageRating(0.0)
                            .totalReviews(0)
                            .hourlyRate(2500.0)
                            .yearsOfExperience(1)
                            .specializations(new ArrayList<>())
                            .serviceAreas(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return repository.save(fallback);
                });
    }

    /**
     * Authenticated technician fetches own full profile.
     */
    public TechnicianProfile getMyProfile(String userId) {
        return getTechnicianById(userId);
    }

    /**
     * Authenticated technician updates own editable professional info.
     * Material changes to qualifications trigger re-verification.
     */
    public TechnicianProfile updateMyProfile(String userId, TechnicianUpdateRequest updateRequest) {
        TechnicianProfile existing = getTechnicianById(userId);

        boolean wasApproved = existing.isApproved();
        boolean qualificationsChanged = false;

        if (updateRequest.getFullName() != null && !updateRequest.getFullName().isBlank()) {
            existing.setFullName(updateRequest.getFullName().trim());
        }
        if (updateRequest.getPhone() != null) {
            existing.setPhone(updateRequest.getPhone().trim());
        }
        if (updateRequest.getCity() != null) {
            existing.setCity(updateRequest.getCity().trim());
        }
        if (updateRequest.getServiceArea() != null) {
            existing.setServiceArea(updateRequest.getServiceArea().trim());
            if (existing.getServiceAreas() == null) existing.setServiceAreas(new ArrayList<>());
            if (!updateRequest.getServiceArea().isBlank() && !existing.getServiceAreas().contains(updateRequest.getServiceArea().trim())) {
                existing.getServiceAreas().add(updateRequest.getServiceArea().trim());
            }
        }
        if (updateRequest.getServiceAreas() != null) {
            existing.setServiceAreas(updateRequest.getServiceAreas());
        }
        if (updateRequest.getProfessionalBio() != null) {
            existing.setProfessionalBio(updateRequest.getProfessionalBio().trim());
        }
        if (updateRequest.getProfileImageUrl() != null) {
            existing.setProfileImageUrl(updateRequest.getProfileImageUrl().trim());
        }
        if (updateRequest.getHourlyRate() != null && updateRequest.getHourlyRate() >= 0) {
            existing.setHourlyRate(updateRequest.getHourlyRate());
        }
        if (updateRequest.getExperienceYears() != null && updateRequest.getExperienceYears() >= 0) {
            existing.setYearsOfExperience(updateRequest.getExperienceYears());
            existing.setExperienceYears(updateRequest.getExperienceYears());
        }
        if (updateRequest.getIsAvailable() != null) {
            existing.setAvailable(updateRequest.getIsAvailable());
        }

        // Specializations update
        if (updateRequest.getSpecializations() != null) {
            List<String> newCanonical = updateRequest.getSpecializations().stream()
                    .map(DeviceSpecialization::normalizeToCanonical)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            List<String> oldSpecs = existing.getSpecializations() != null ? existing.getSpecializations() : Collections.emptyList();
            if (!new HashSet<>(oldSpecs).equals(new HashSet<>(newCanonical))) {
                qualificationsChanged = true;
                existing.setSpecializations(newCanonical);
            }
        }

        // Policy: Materially modified qualifications on an approved profile trigger re-verification
        if (wasApproved && qualificationsChanged) {
            existing.setVerificationStatus(VerificationStatus.PENDING.name());
            existing.setRejectionReason(null);
            log.info("Technician {} modified specializations; status transitioned from APPROVED to PENDING for re-verification", userId);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    /**
     * Upload credentials document (PDF/PNG/JPG up to 5MB).
     * Transitions profile to PENDING for admin review.
     */
    public CertificateUploadResponse uploadCertificate(String userId, MultipartFile file) throws IOException {
        certificateValidator.validate(file);

        TechnicianProfile profile = getTechnicianById(userId);

        StoredDocument stored = documentStorageService.uploadDocument(file, userId, "CERTIFICATE");

        profile.setCertificateUrl(stored.getUrl());
        profile.setCertificateFilename(stored.getFilename());
        profile.setCertificateUploadedAt(LocalDateTime.now());
        profile.setVerificationStatus(VerificationStatus.PENDING.name());
        profile.setRejectionReason(null);
        profile.setUpdatedAt(LocalDateTime.now());

        repository.save(profile);
        log.info("Certificate uploaded for technician {}. Profile status set to PENDING.", userId);

        return CertificateUploadResponse.builder()
                .certificateUrl(stored.getUrl())
                .certificateFilename(stored.getFilename())
                .uploadedAt(profile.getCertificateUploadedAt())
                .verificationStatus(profile.getVerificationStatus())
                .message("Certificate uploaded successfully. Your profile is now pending admin verification.")
                .build();
    }

    /**
     * Public listing: ONLY returns approved technicians with sensitive fields sanitized.
     */
    public List<TechnicianProfile> getAllTechnicians() {
        return getAllPublicTechnicians(null);
    }

    public List<TechnicianProfile> getAllPublicTechnicians(String specialization) {
        List<TechnicianProfile> approvedTechs = repository.findByVerificationStatusIn(APPROVED_STATUSES);

        if (specialization != null && !specialization.isBlank()) {
            return approvedTechs.stream()
                    .filter(t -> DeviceSpecialization.matchesCategory(t.getSpecializations(), specialization))
                    .map(this::sanitizeForPublic)
                    .collect(Collectors.toList());
        }

        return approvedTechs.stream()
                .map(this::sanitizeForPublic)
                .collect(Collectors.toList());
    }

    /**
     * Search technicians with optional filters.
     * Public search defaults to APPROVED-only unless explicitly overridden by admin.
     */
    public List<TechnicianProfile> searchTechnicians(String specialization, String serviceArea, Boolean available, String status) {
        List<TechnicianProfile> pool;

        if (status != null && !status.isBlank()) {
            pool = repository.findByVerificationStatus(status.trim().toUpperCase());
        } else {
            // Default to approved only for security
            pool = repository.findByVerificationStatusIn(APPROVED_STATUSES);
        }

        return pool.stream()
                .filter(t -> available == null || Objects.equals(t.getIsAvailable(), available))
                .filter(t -> serviceArea == null || serviceArea.isBlank() || matchesServiceArea(t, serviceArea))
                .filter(t -> specialization == null || specialization.isBlank() || DeviceSpecialization.matchesCategory(t.getSpecializations(), specialization))
                .map(this::sanitizeForPublic)
                .collect(Collectors.toList());
    }

    private TechnicianProfile sanitizeForPublic(TechnicianProfile original) {
        if (original == null) return null;
        TechnicianProfile copy = TechnicianProfile.builder()
                .id(original.getId())
                .fullName(original.getFullName())
                .email(original.getEmail())
                .phone(original.getPhone())
                .specializations(original.getSpecializations())
                .serviceAreas(original.getServiceAreas())
                .city(original.getCity())
                .serviceArea(original.getServiceArea())
                .professionalBio(original.getProfessionalBio())
                .available(original.getAvailable())
                .verificationStatus(original.getVerificationStatus())
                .averageRating(original.getAverageRating())
                .totalReviews(original.getTotalReviews())
                .hourlyRate(original.getHourlyRate())
                .yearsOfExperience(original.getYearsOfExperience())
                .profileImageUrl(original.getProfileImageUrl())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();
        copy.setCertificateUrl(null);
        copy.setCertificateFilename(null);
        copy.setCertificateUploadedAt(null);
        copy.setRejectionReason(null);
        copy.setReviewedBy(null);
        copy.setReviewedAt(null);
        return copy;
    }

    private boolean matchesServiceArea(TechnicianProfile t, String area) {
        String aLower = area.trim().toLowerCase();
        if (t.getCity() != null && t.getCity().toLowerCase().contains(aLower)) return true;
        if (t.getServiceArea() != null && t.getServiceArea().toLowerCase().contains(aLower)) return true;
        if (t.getServiceAreas() != null) {
            return t.getServiceAreas().stream().anyMatch(sa -> sa != null && sa.toLowerCase().contains(aLower));
        }
        return false;
    }

    /**
     * Admin review action: APPROVED, REJECTED, SUSPENDED, PENDING.
     */
    public TechnicianProfile updateVerificationStatus(String technicianId, String adminId, String newStatus, String reason) {
        TechnicianProfile profile = getTechnicianById(technicianId);
        VerificationStatus target = VerificationStatus.fromString(newStatus);

        profile.setVerificationStatus(target.name());
        profile.setReviewedBy(adminId);
        profile.setReviewedAt(LocalDateTime.now());
        if (target == VerificationStatus.REJECTED) {
            profile.setRejectionReason(reason != null && !reason.isBlank() ? reason.trim() : "Credentials did not meet verification criteria");
        } else {
            profile.setRejectionReason(null);
        }
        profile.setUpdatedAt(LocalDateTime.now());

        TechnicianProfile updated = repository.save(profile);
        log.info("Admin {} updated technician {} status to {}", adminId, technicianId, target);
        return updated;
    }

    public TechnicianProfile setAvailability(String id, boolean available) {
        TechnicianProfile existing = getTechnicianById(id);
        existing.setAvailable(available);
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    /**
     * Backward-compatible update method used by existing internal clients (e.g. AdminClient).
     */
    public TechnicianProfile updateTechnician(String id, TechnicianProfile updateRequest) {
        TechnicianProfile existing = getTechnicianById(id);
        if (updateRequest.getFullName() != null) existing.setFullName(updateRequest.getFullName());
        if (updateRequest.getEmail() != null) existing.setEmail(updateRequest.getEmail());
        if (updateRequest.getPhone() != null) existing.setPhone(updateRequest.getPhone());
        if (updateRequest.getSpecializations() != null) existing.setSpecializations(updateRequest.getSpecializations());
        if (updateRequest.getServiceAreas() != null) existing.setServiceAreas(updateRequest.getServiceAreas());
        if (updateRequest.getVerificationStatus() != null) existing.setVerificationStatus(updateRequest.getVerificationStatus());
        if (updateRequest.getHourlyRate() != null) existing.setHourlyRate(updateRequest.getHourlyRate());
        if (updateRequest.getYearsOfExperience() != null) existing.setYearsOfExperience(updateRequest.getYearsOfExperience());
        if (updateRequest.getProfileImageUrl() != null) existing.setProfileImageUrl(updateRequest.getProfileImageUrl());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }
}
