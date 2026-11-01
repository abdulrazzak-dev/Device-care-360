package com.devicecare360.technician.controller;

import com.devicecare360.shared.dto.ApiResponse;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.dto.AvailabilityUpdateRequest;
import com.devicecare360.technician.dto.TechnicianProfileDTO;
import com.devicecare360.technician.service.TechnicianService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianControllerTest {

    @Mock
    private TechnicianService technicianService;

    @InjectMocks
    private TechnicianController technicianController;

    @Test
    @DisplayName("GET /api/technicians returns list of all public technicians")
    void testGetAllTechnicians() {
        TechnicianProfile sample = TechnicianProfile.builder()
                .id("tech-001")
                .fullName("Kasun Perera")
                .hourlyRate(2500.0)
                .yearsOfExperience(7)
                .available(true)
                .specializations(List.of("Smartphone Repair"))
                .serviceAreas(List.of("Colombo"))
                .build();

        when(technicianService.getAllTechnicians()).thenReturn(List.of(sample));

        ResponseEntity<ApiResponse<List<TechnicianProfile>>> response = technicianController.getAll(null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Kasun Perera", response.getBody().getData().get(0).getFullName());
        verify(technicianService, times(1)).getAllTechnicians();
    }

    @Test
    @DisplayName("GET /api/technicians with specialization param searches by specialization")
    void testGetTechniciansWithSpecializationParam() {
        TechnicianProfile sample = TechnicianProfile.builder()
                .id("tech-001")
                .fullName("Kasun Perera")
                .available(true)
                .specializations(List.of("Smartphone Repair"))
                .build();

        when(technicianService.getAllPublicTechnicians("Smartphone Repair"))
                .thenReturn(List.of(sample));

        ResponseEntity<ApiResponse<List<TechnicianProfile>>> response = technicianController.getAll("Smartphone Repair");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().size());
        verify(technicianService, times(1)).getAllPublicTechnicians("Smartphone Repair");
    }

    @Test
    @DisplayName("PATCH /api/technicians/me/availability switches to Available (true)")
    void testUpdateMyAvailabilityToTrue() {
        String techId = "tech-001";
        AvailabilityUpdateRequest request = AvailabilityUpdateRequest.builder()
                .isAvailable(true)
                .build();

        TechnicianProfileDTO expectedDTO = TechnicianProfileDTO.builder()
                .id(techId)
                .fullName("Kasun Perera")
                .isAvailable(true)
                .verificationStatus("APPROVED")
                .build();

        when(technicianService.updateAvailability(techId, true)).thenReturn(expectedDTO);

        ResponseEntity<ApiResponse<TechnicianProfileDTO>> response = technicianController.updateMyAvailability(
                techId, "TECHNICIAN", null, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(Boolean.TRUE, response.getBody().getData().getIsAvailable());
        assertEquals("APPROVED", response.getBody().getData().getVerificationStatus());
        verify(technicianService, times(1)).updateAvailability(techId, true);
    }

    @Test
    @DisplayName("PATCH /api/technicians/me/availability switches to Offline (false)")
    void testUpdateMyAvailabilityToFalse() {
        String techId = "tech-001";
        AvailabilityUpdateRequest request = AvailabilityUpdateRequest.builder()
                .isAvailable(false)
                .build();

        TechnicianProfileDTO expectedDTO = TechnicianProfileDTO.builder()
                .id(techId)
                .fullName("Kasun Perera")
                .isAvailable(false)
                .verificationStatus("APPROVED")
                .build();

        when(technicianService.updateAvailability(techId, false)).thenReturn(expectedDTO);

        ResponseEntity<ApiResponse<TechnicianProfileDTO>> response = technicianController.updateMyAvailability(
                techId, "ROLE_TECHNICIAN", null, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(Boolean.FALSE, response.getBody().getData().getIsAvailable());
        verify(technicianService, times(1)).updateAvailability(techId, false);
    }

    @Test
    @DisplayName("PATCH /api/technicians/me/availability rejects unauthenticated request with 401")
    void testUpdateMyAvailabilityUnauthenticatedRejection() {
        AvailabilityUpdateRequest request = AvailabilityUpdateRequest.builder()
                .isAvailable(true)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                technicianController.updateMyAvailability(null, null, null, request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(technicianService, never()).updateAvailability(any(), anyBoolean());
    }

    @Test
    @DisplayName("PATCH /api/technicians/me/availability rejects non-technician user (USER) with 403")
    void testUpdateMyAvailabilityUnauthorizedRoleRejection() {
        AvailabilityUpdateRequest request = AvailabilityUpdateRequest.builder()
                .isAvailable(true)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                technicianController.updateMyAvailability("user-123", "ROLE_USER", null, request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(technicianService, never()).updateAvailability(any(), anyBoolean());
    }

    @Test
    @DisplayName("PUT /api/technicians/{id}/availability prevents one technician updating another technician's availability")
    void testOneTechnicianCannotUpdateAnotherTechniciansAvailability() {
        String loggedInTechId = "tech-001";
        String victimTechId = "tech-002";

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                technicianController.setAvailability(victimTechId, false, loggedInTechId, "TECHNICIAN", null));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(technicianService, never()).updateAvailability(any(), anyBoolean());
    }

    @Test
    @DisplayName("PUT /api/technicians/{id}/availability allows ADMIN to update technician's availability")
    void testAdminCanUpdateAnyTechniciansAvailability() {
        String adminId = "admin-999";
        String targetTechId = "tech-002";

        TechnicianProfileDTO expectedDTO = TechnicianProfileDTO.builder()
                .id(targetTechId)
                .isAvailable(true)
                .verificationStatus("APPROVED")
                .build();

        when(technicianService.updateAvailability(targetTechId, true)).thenReturn(expectedDTO);

        ResponseEntity<ApiResponse<TechnicianProfileDTO>> response = technicianController.setAvailability(
                targetTechId, true, adminId, "ROLE_ADMIN", null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        verify(technicianService, times(1)).updateAvailability(targetTechId, true);
    }
}
