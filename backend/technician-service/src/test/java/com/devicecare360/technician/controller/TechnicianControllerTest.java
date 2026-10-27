package com.devicecare360.technician.controller;

import com.devicecare360.shared.dto.ApiResponse;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.service.TechnicianService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
    @DisplayName("GET /api/technicians returns list of all technicians")
    void testGetAllTechnicians() {
        TechnicianProfile sample = TechnicianProfile.builder()
                .id("tech-001")
                .fullName("Kasun Perera")
                .hourlyRate(2500.0)
                .yearsOfExperience(7)
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
                .specializations(List.of("Smartphone Repair"))
                .build();

        when(technicianService.searchTechnicians("Smartphone Repair", null, null, null))
                .thenReturn(List.of(sample));

        ResponseEntity<ApiResponse<List<TechnicianProfile>>> response = technicianController.getAll("Smartphone Repair");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().size());
        verify(technicianService, times(1)).searchTechnicians("Smartphone Repair", null, null, null);
    }
}
