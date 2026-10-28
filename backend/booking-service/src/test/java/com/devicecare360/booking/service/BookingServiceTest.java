package com.devicecare360.booking.service;

import com.devicecare360.booking.client.TechnicianClient;
import com.devicecare360.booking.document.Booking;
import com.devicecare360.booking.repository.BookingRepository;
import com.devicecare360.shared.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repository;

    @Mock
    private TechnicianClient technicianClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("Create booking sets status PENDING, populates tech name, and persists in repository")
    void testCreateBookingSuccess() {
        Booking input = Booking.builder()
                .userId("user-123")
                .technicianId("tech-001")
                .deviceCategory("Smartphone")
                .deviceModel("Apple iPhone 13 Pro Max")
                .issueDescription("Screen broken")
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .estimatedCost(new BigDecimal("2500.00"))
                .build();

        Map<String, Object> techMap = Map.of(
                "id", "tech-001",
                "fullName", "Kasun Perera",
                "available", true,
                "verificationStatus", "APPROVED",
                "specializations", List.of("SMARTPHONE_MOBILE"),
                "hourlyRate", 2500.0
        );
        when(technicianClient.getTechnicianById("tech-001")).thenReturn(ApiResponse.success(techMap));
        when(repository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("booking-xyz-999");
            return b;
        });

        Booking result = bookingService.createBooking(input);

        assertNotNull(result);
        assertEquals("booking-xyz-999", result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals("user-123", result.getUserId());
        assertEquals("tech-001", result.getTechnicianId());
        assertEquals("Kasun Perera", result.getTechnicianName());
        assertEquals("Apple iPhone 13 Pro Max", result.getDeviceModel());
        assertNotNull(result.getCreatedAt());

        verify(repository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Create booking rejects unapproved technician with IllegalStateException")
    void testRejectUnapprovedTechnician() {
        Booking input = Booking.builder()
                .userId("user-123")
                .technicianId("tech-002")
                .deviceCategory("Smartphone")
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .build();

        Map<String, Object> techMap = Map.of(
                "id", "tech-002",
                "fullName", "Pending Tech",
                "available", true,
                "verificationStatus", "PENDING"
        );
        when(technicianClient.getTechnicianById("tech-002")).thenReturn(ApiResponse.success(techMap));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bookingService.createBooking(input));
        assertTrue(ex.getMessage().contains("not approved"));
    }

    @Test
    @DisplayName("Create booking rejects offline/unavailable technician")
    void testRejectUnavailableTechnician() {
        Booking input = Booking.builder()
                .userId("user-123")
                .technicianId("tech-003")
                .deviceCategory("Smartphone")
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .build();

        Map<String, Object> techMap = Map.of(
                "id", "tech-003",
                "fullName", "Offline Tech",
                "available", false,
                "verificationStatus", "APPROVED"
        );
        when(technicianClient.getTechnicianById("tech-003")).thenReturn(ApiResponse.success(techMap));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bookingService.createBooking(input));
        assertTrue(ex.getMessage().contains("unavailable"));
    }

    @Test
    @DisplayName("Create booking rejects technician that does not support the device category")
    void testRejectUnsupportedCategory() {
        Booking input = Booking.builder()
                .userId("user-123")
                .technicianId("tech-004")
                .deviceCategory("Refrigerator")
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .build();

        Map<String, Object> techMap = Map.of(
                "id", "tech-004",
                "fullName", "Phone Tech",
                "available", true,
                "verificationStatus", "APPROVED",
                "specializations", List.of("SMARTPHONE_MOBILE")
        );
        when(technicianClient.getTechnicianById("tech-004")).thenReturn(ApiResponse.success(techMap));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(input));
        assertTrue(ex.getMessage().contains("does not support repairs for category"));
    }

    @Test
    @DisplayName("Create booking rejects past appointment date")
    void testRejectPastAppointment() {
        Booking input = Booking.builder()
                .userId("user-123")
                .technicianId("tech-001")
                .deviceCategory("Smartphone")
                .appointmentTime(LocalDateTime.now().minusHours(2))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(input));
        assertTrue(ex.getMessage().contains("future"));
    }
}
