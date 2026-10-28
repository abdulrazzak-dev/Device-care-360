package com.devicecare360.technician.service;

import com.devicecare360.shared.model.DeviceSpecialization;
import com.devicecare360.technician.document.TechnicianProfile;
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
    @DisplayName("Public listing excludes unapproved (PENDING/REJECTED/SUSPENDED) technicians")
    void testPublicListingExcludesUnapproved() {
        TechnicianProfile approved = TechnicianProfile.builder()
                .id("tech-1")
                .fullName("Approved Tech")
                .verificationStatus("APPROVED")
                .build();

        when(repository.findByVerificationStatusIn(anyCollection())).thenReturn(List.of(approved));

        List<TechnicianProfile> publicList = technicianService.getAllPublicTechnicians(null);

        assertEquals(1, publicList.size());
        assertEquals("Approved Tech", publicList.get(0).getFullName());
        assertNull(publicList.get(0).getCertificateUrl(), "Sensitive document URLs must be scrubbed in public listings");
    }
}
