package com.devicecare360.technician.config;

import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.repository.TechnicianProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private TechnicianProfileRepository technicianRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    @DisplayName("When database is empty (count=0), seeder persists four sample technicians")
    void testSeedWhenDatabaseEmpty() throws Exception {
        when(technicianRepository.count()).thenReturn(0L);
        when(technicianRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TechnicianProfile>> captor = ArgumentCaptor.forClass(List.class);
        verify(technicianRepository, times(1)).saveAll(captor.capture());

        List<TechnicianProfile> seeded = captor.getValue();
        assertEquals(4, seeded.size(), "Should seed exactly four sample technicians");

        // Profile 1: Kasun Perera (Smartphone Repair, 7 years, LKR 2500, Colombo)
        TechnicianProfile kasun = seeded.get(0);
        assertEquals("Kasun Perera", kasun.getFullName());
        assertTrue(kasun.getSpecializations().contains("Smartphone Repair"));
        assertEquals(7, kasun.getYearsOfExperience());
        assertEquals(4.9, kasun.getAverageRating());
        assertEquals(2500.0, kasun.getHourlyRate());
        assertTrue(kasun.getServiceAreas().contains("Colombo"));
        assertTrue(kasun.isAvailable());
        assertEquals("VERIFIED", kasun.getVerificationStatus());
        assertNotNull(kasun.getProfileImageUrl());

        // Profile 2: Mohamed Rizwan (Laptop/PC Hardware, 9 years, LKR 3000, Negombo)
        TechnicianProfile rizwan = seeded.get(1);
        assertEquals("Mohamed Rizwan", rizwan.getFullName());
        assertTrue(rizwan.getSpecializations().contains("Laptop/PC Hardware"));
        assertEquals(9, rizwan.getYearsOfExperience());
        assertEquals(4.8, rizwan.getAverageRating());
        assertEquals(3000.0, rizwan.getHourlyRate());
        assertTrue(rizwan.getServiceAreas().contains("Negombo"));

        // Profile 3: Nimal Fernando (Audio/Video Gear, 6 years, LKR 2000, Gampaha)
        TechnicianProfile nimal = seeded.get(2);
        assertEquals("Nimal Fernando", nimal.getFullName());
        assertTrue(nimal.getSpecializations().contains("Audio/Video Gear"));
        assertEquals(6, nimal.getYearsOfExperience());
        assertEquals(4.7, nimal.getAverageRating());
        assertEquals(2000.0, nimal.getHourlyRate());
        assertTrue(nimal.getServiceAreas().contains("Gampaha"));

        // Profile 4: Sahan Jayawardena (Smart Home Appliances, 8 years, LKR 2800, Kandy)
        TechnicianProfile sahan = seeded.get(3);
        assertEquals("Sahan Jayawardena", sahan.getFullName());
        assertTrue(sahan.getSpecializations().contains("Smart Home Appliances"));
        assertEquals(8, sahan.getYearsOfExperience());
        assertEquals(4.9, sahan.getAverageRating());
        assertEquals(2800.0, sahan.getHourlyRate());
        assertTrue(sahan.getServiceAreas().contains("Kandy"));
    }

    @Test
    @DisplayName("When database already has technician records (count > 0), seeder skips without modifying")
    void testSkipWhenDatabaseHasRecords() throws Exception {
        when(technicianRepository.count()).thenReturn(5L);

        dataSeeder.run();

        verify(technicianRepository, never()).saveAll(anyList());
        verify(technicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("When database throws connectivity exception, seeder propagates error rather than swallowing it")
    void testPropagateExceptionOnDatabaseError() {
        when(technicianRepository.count()).thenThrow(new RuntimeException("MongoDB connection timeout"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> dataSeeder.run());
        assertTrue(thrown.getMessage().contains("MongoDB connection timeout"));
    }
}
