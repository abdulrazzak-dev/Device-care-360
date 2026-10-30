package com.devicecare360.repairguide.service;

import com.devicecare360.repairguide.document.RepairGuide;
import com.devicecare360.repairguide.repository.RepairGuideRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairGuideServiceTest {

    @Mock
    private RepairGuideRepository repository;

    @InjectMocks
    private RepairGuideService service;

    @Test
    @DisplayName("Should return only approved repair guides and exclude unapproved ones")
    void testGetAllGuides_FilterApproved() {
        RepairGuide approvedGuide = RepairGuide.builder().id("1").title("Screen Fix").approved(true).build();
        RepairGuide legacyGuide = RepairGuide.builder().id("2").title("Battery Fix").approved(null).build();
        RepairGuide unapprovedGuide = RepairGuide.builder().id("3").title("Draft Fix").approved(false).build();

        when(repository.findAll()).thenReturn(List.of(approvedGuide, legacyGuide, unapprovedGuide));

        List<RepairGuide> result = service.getAllGuides(null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RepairGuide::getId).containsExactly("1", "2");
    }

    @Test
    @DisplayName("Should query by category ignoring case and 'ALL' sentinel")
    void testGetAllGuides_CategoryFilter() {
        RepairGuide acGuide = RepairGuide.builder().id("1").title("AC Leak").category("Air Conditioner").approved(true).build();
        when(repository.findByCategoryIgnoreCase("air conditioner")).thenReturn(List.of(acGuide));

        List<RepairGuide> result = service.getAllGuides("air conditioner", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("AC Leak");
        verify(repository).findByCategoryIgnoreCase("air conditioner");
    }

    @Test
    @DisplayName("Should retrieve guide by ID or throw when not found")
    void testGetGuideById() {
        RepairGuide guide = RepairGuide.builder().id("123").title("Fan Clean").build();
        when(repository.findById("123")).thenReturn(Optional.of(guide));
        when(repository.findById("999")).thenReturn(Optional.empty());

        RepairGuide found = service.getGuideById("123");
        assertThat(found.getTitle()).isEqualTo("Fan Clean");

        assertThrows(IllegalArgumentException.class, () -> service.getGuideById("999"));
    }

    @Test
    @DisplayName("Should synchronize aliases when creating a guide")
    void testCreateGuide_SynchronizesAliases() {
        RepairGuide input = RepairGuide.builder()
                .title("New Guide")
                .difficulty("EASY")
                .description("Sample issue")
                .steps(List.of("Step 1"))
                .build();

        when(repository.save(any(RepairGuide.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepairGuide saved = service.createGuide(input);

        assertThat(saved.getSkillLevel()).isEqualTo("EASY");
        assertThat(saved.getIssue()).isEqualTo("Sample issue");
        assertThat(saved.getSafeInstructions()).containsExactly("Step 1");
        assertThat(saved.getApproved()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
