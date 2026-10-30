package com.devicecare360.repairguide.config;

import com.devicecare360.repairguide.document.RepairGuide;
import com.devicecare360.repairguide.repository.RepairGuideRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairGuideDataSeederTest {

    @Mock
    private RepairGuideRepository repairGuideRepository;

    @InjectMocks
    private RepairGuideDataSeeder seeder;

    @Test
    @DisplayName("Should seed exactly 5 approved repair guides when repository is empty")
    void testSeedWhenDatabaseEmpty() {
        when(repairGuideRepository.count()).thenReturn(0L);

        seeder.run();

        ArgumentCaptor<List<RepairGuide>> captor = ArgumentCaptor.forClass(List.class);
        verify(repairGuideRepository, times(1)).saveAll(captor.capture());

        List<RepairGuide> seeded = captor.getValue();
        assertThat(seeded).hasSize(5);

        // Verify each guide has required fields and approved=true
        for (RepairGuide guide : seeded) {
            assertThat(guide.getTitle()).isNotBlank();
            assertThat(guide.getCategory()).isNotBlank();
            assertThat(guide.getApproved()).isTrue();
            assertThat(guide.getRiskLevel()).isIn("LOW", "MEDIUM", "HIGH", "CRITICAL");
            assertThat(guide.getSkillLevel()).isNotBlank();
            assertThat(guide.getToolsRequired()).isNotEmpty();
            assertThat(guide.getSafeInstructions()).isNotEmpty();
            assertThat(guide.getSafetyWarnings()).isNotEmpty();
            assertThat(guide.getEstimatedTime()).isNotBlank();
            assertThat(guide.getCreatedAt()).isNotNull();
        }

        // Verify specific canonical categories seeded
        List<String> categories = seeded.stream().map(RepairGuide::getCategory).toList();
        assertThat(categories).contains("Smartphone", "Laptop / PC", "Air Conditioner", "Washing Machine", "Refrigerator");
    }

    @Test
    @DisplayName("Should skip seeding when database already contains repair guides")
    void testSkipSeedWhenDatabaseNotEmpty() {
        when(repairGuideRepository.count()).thenReturn(5L);

        seeder.run();

        verify(repairGuideRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle database connection failures gracefully without throwing")
    void testHandleDatabaseExceptionGracefully() {
        when(repairGuideRepository.count()).thenThrow(new RuntimeException("MongoDB connection timeout"));

        // Seeder must catch exception and log visibly without crashing application startup
        seeder.run();

        verify(repairGuideRepository, never()).saveAll(anyList());
    }
}
