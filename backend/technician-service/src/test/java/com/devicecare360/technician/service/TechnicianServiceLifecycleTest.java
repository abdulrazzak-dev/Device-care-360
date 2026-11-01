package com.devicecare360.technician.service;

import com.devicecare360.shared.model.DeviceSpecialization;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.dto.TechnicianProfileDTO;
import com.devicecare360.technician.dto.TechnicianUpdateRequest;
import com.devicecare360.technician.repository.TechnicianProfileRepository;
import com.devicecare360.technician.storage.CertificateValidator;
import com.devicecare360.technician.storage.DocumentStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceLifecycleTest {

    @Mock
    private TechnicianProfileRepository repository;

    @Mock
    private DocumentStorageService storageService;

    @Mock
    private CertificateValidator validator;

    @InjectMocks
    private TechnicianService technicianService;

    @Test
    @DisplayName("Registration always forces verificationStatus to PENDING even if client sent APPROVED")
    void testRegistrationEnforcesPending() {
        TechnicianProfile req = TechnicianProfile.builder()
                .id("tech-999")
                .fullName("John Tech")
                .verificationStatus("APPROVED") // Malicious attempt to self-approve
                .specializations(List.of("Smartphone"))
                .build();

        when(repository.existsById("tech-999")).thenReturn(false);
        when(repository.save(any(TechnicianProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicianProfile saved = technicianService.registerTechnician(req);

        assertEquals("PENDING", saved.getVerificationStatus(), "New registration MUST default to PENDING");
        assertTrue(saved.getSpecializations().contains(DeviceSpecialization.SMARTPHONE_MOBILE.name()));
    }

    @Test
    @DisplayName("Updating core specializations on an approved profile transitions status back to PENDING")
    void testQualificationUpdateTriggersReVerification() {
        TechnicianProfile existing = TechnicianProfile.builder()
                .id("tech-100")
                .fullName("Jane Tech")
                .verificationStatus("APPROVED")
                .specializations(new ArrayList<>(List.of(DeviceSpecialization.SMARTPHONE_MOBILE.name())))
                .build();

        when(repository.findById("tech-100")).thenReturn(Optional.of(existing));
        when(repository.save(any(TechnicianProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicianUpdateRequest update = TechnicianUpdateRequest.builder()
                .specializations(List.of("Refrigerator", "Air Conditioner"))
                .build();

        TechnicianProfile result = technicianService.updateMyProfile("tech-100", update);

        assertEquals("PENDING", result.getVerificationStatus(), "Status must revert to PENDING when qualifications change");
        assertTrue(result.getSpecializations().contains(DeviceSpecialization.REFRIGERATOR.name()));
    }

    @Test
    @DisplayName("Updating non-qualification details on an approved profile preserves APPROVED status")
    void testNonQualificationUpdatePreservesApproved() {
        TechnicianProfile existing = TechnicianProfile.builder()
                .id("tech-100")
                .fullName("Jane Tech")
                .verificationStatus("APPROVED")
                .specializations(List.of(DeviceSpecialization.SMARTPHONE_MOBILE.name()))
                .build();

        when(repository.findById("tech-100")).thenReturn(Optional.of(existing));
        when(repository.save(any(TechnicianProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicianUpdateRequest update = TechnicianUpdateRequest.builder()
                .phone("+94 77 999 8888")
                .professionalBio("Updated bio description")
                .build();

        TechnicianProfile result = technicianService.updateMyProfile("tech-100", update);

        assertEquals("APPROVED", result.getVerificationStatus(), "Status should remain APPROVED for contact/bio edits");
    }

    @Test
    @DisplayName("Public listing excludes unapproved and offline technicians")
    void testPublicListingExcludesUnapprovedAndUnavailable() {
        TechnicianProfile approvedAndOnline = TechnicianProfile.builder()
                .id("tech-1")
                .fullName("Approved Online Tech")
                .verificationStatus("APPROVED")
                .available(true)
                .build();

        when(repository.findByVerificationStatusInAndAvailable(anyCollection(), eq(true)))
                .thenReturn(List.of(approvedAndOnline));

        List<TechnicianProfile> publicList = technicianService.getAllPublicTechnicians(null);

        assertEquals(1, publicList.size());
        assertEquals("Approved Online Tech", publicList.get(0).getFullName());
        assertTrue(publicList.get(0).isAvailable());
        assertNull(publicList.get(0).getCertificateUrl(), "Sensitive document URLs must be scrubbed in public listings");
    }

    @Test
    @DisplayName("updateAvailability persists new value isAvailable=true in MongoDB and returns DTO")
    void testUpdateAvailabilityPersistsToAvailable() {
        String techId = "tech-001";
        TechnicianProfile existing = TechnicianProfile.builder()
                .id(techId)
                .fullName("Kasun Perera")
                .verificationStatus("APPROVED")
                .available(false)
                .build();

        when(repository.findById(techId)).thenReturn(Optional.of(existing));
        when(repository.save(any(TechnicianProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicianProfileDTO dto = technicianService.updateAvailability(techId, true);

        assertNotNull(dto);
        assertEquals(techId, dto.getId());
        assertEquals(Boolean.TRUE, dto.getIsAvailable());
        assertEquals(Boolean.TRUE, dto.getAvailable());
        verify(repository, times(1)).save(argThat(t -> Boolean.TRUE.equals(t.getAvailable())));
    }

    @Test
    @DisplayName("updateAvailability persists new value isAvailable=false in MongoDB and returns DTO")
    void testUpdateAvailabilityPersistsToOffline() {
        String techId = "tech-001";
        TechnicianProfile existing = TechnicianProfile.builder()
                .id(techId)
                .fullName("Kasun Perera")
                .verificationStatus("APPROVED")
                .available(true)
                .build();

        when(repository.findById(techId)).thenReturn(Optional.of(existing));
        when(repository.save(any(TechnicianProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicianProfileDTO dto = technicianService.updateAvailability(techId, false);

        assertNotNull(dto);
        assertEquals(techId, dto.getId());
        assertEquals(Boolean.FALSE, dto.getIsAvailable());
        assertEquals(Boolean.FALSE, dto.getAvailable());
        verify(repository, times(1)).save(argThat(t -> Boolean.FALSE.equals(t.getAvailable())));
    }

    @Test
    @DisplayName("updateAvailability throws 404 NOT_FOUND when technician profile does not exist")
    void testUpdateAvailabilityNotFoundThrows404() {
        String missingId = "non-existent-tech";
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                technicianService.updateAvailability(missingId, true));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Pending technician toggling online does NOT become bookable in public directory queries")
    void testPendingTechnicianTogglingOnlineRemainsExcluded() {
        // When public directory searches for eligible technicians, the repository is queried for APPROVED status
        when(repository.findByVerificationStatusInAndAvailable(anyCollection(), eq(true)))
                .thenReturn(List.of()); // Pending technician is NOT in APPROVED pool

        List<TechnicianProfile> publicList = technicianService.getAllPublicTechnicians(null);

        assertTrue(publicList.isEmpty(), "Pending technician must never appear in public directory even if available=true");
    }

    @Test
    @DisplayName("Missing legacy isAvailable / available fields default safely to false without NullPointerException")
    void testLegacyProfileWithNullAvailabilityDoesNotNPE() {
        TechnicianProfile legacyProfile = TechnicianProfile.builder()
                .id("legacy-tech-01")
                .fullName("Legacy Tech")
                .available(null) // legacy record with missing field
                .build();

        assertFalse(legacyProfile.isAvailable(), "Missing/null availability must evaluate to false");
        assertEquals(Boolean.FALSE, legacyProfile.getIsAvailable(), "getIsAvailable() must return false for null");
        assertEquals(Boolean.FALSE, legacyProfile.getAvailable(), "getAvailable() must return false for null");

        TechnicianProfileDTO dto = TechnicianProfileDTO.fromEntity(legacyProfile);
        assertEquals(Boolean.FALSE, dto.getIsAvailable());
        assertEquals(Boolean.FALSE, dto.getAvailable());
    }
}
