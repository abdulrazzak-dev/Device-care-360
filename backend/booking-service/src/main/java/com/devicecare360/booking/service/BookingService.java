package com.devicecare360.booking.service;

import com.devicecare360.booking.client.TechnicianClient;
import com.devicecare360.booking.config.RabbitMQConfig;
import com.devicecare360.booking.document.Booking;
import com.devicecare360.booking.repository.BookingRepository;
import com.devicecare360.shared.event.*;
import com.devicecare360.shared.model.DeviceSpecialization;
import com.devicecare360.shared.model.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository repository;
    private final TechnicianClient technicianClient;
    private final RabbitTemplate rabbitTemplate;

    public Booking createBooking(Booking booking) {
        if (booking.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Appointment date and time must be provided.");
        }
        if (booking.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date and time must be scheduled in the future.");
        }

        if (booking.getTechnicianId() == null || booking.getTechnicianId().isBlank()) {
            throw new IllegalArgumentException("A certified technician must be selected for the booking.");
        }

        try {
            var techResp = technicianClient.getTechnicianById(booking.getTechnicianId());
            if (techResp == null || techResp.getData() == null) {
                throw new IllegalArgumentException("Selected technician not found for ID: " + booking.getTechnicianId());
            }

            Map<String, Object> techData = techResp.getData();

            // 1. Verify technician approval status
            String status = (String) techData.get("verificationStatus");
            if (!VerificationStatus.isApproved(status)) {
                throw new IllegalStateException("Technician is not approved for receiving repair bookings. Current status: " + (status != null ? status : "PENDING"));
            }

            // 2. Verify technician availability
            Boolean available = (Boolean) techData.get("available");
            if (available == null && techData.get("isAvailable") != null) {
                available = (Boolean) techData.get("isAvailable");
            }
            if (available != null && !available) {
                throw new IllegalStateException("Selected technician is currently offline or unavailable for appointments.");
            }

            // 3. Verify device category match if category is specified
            if (booking.getDeviceCategory() != null && !booking.getDeviceCategory().isBlank()) {
                @SuppressWarnings("unchecked")
                List<String> specs = (List<String>) techData.get("specializations");
                if (specs == null) {
                    @SuppressWarnings("unchecked")
                    List<String> cats = (List<String>) techData.get("categories");
                    specs = cats;
                }
                if (!DeviceSpecialization.matchesCategory(specs, booking.getDeviceCategory())) {
                    throw new IllegalArgumentException("Selected technician does not support repairs for category: " + booking.getDeviceCategory());
                }
            }

            // 4. Populate technician details and server-authoritative rate
            if (techData.get("fullName") != null) {
                booking.setTechnicianName((String) techData.get("fullName"));
            }
            Object rateObj = techData.get("hourlyRate");
            if (rateObj instanceof Number) {
                booking.setEstimatedCost(BigDecimal.valueOf(((Number) rateObj).doubleValue()));
            } else if (booking.getEstimatedCost() == null || booking.getEstimatedCost().compareTo(BigDecimal.ZERO) <= 0) {
                booking.setEstimatedCost(BigDecimal.valueOf(75.00));
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Booking integrity validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify technician via client: {}", e.getMessage());
            throw new IllegalStateException("Unable to verify technician eligibility at this time. Please try again.");
        }

        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking saved = repository.save(booking);

        try {
            BookingCreatedEvent event = BookingCreatedEvent.builder()
                    .bookingId(saved.getId())
                    .userId(saved.getUserId())
                    .technicianId(saved.getTechnicianId())
                    .deviceId(saved.getDeviceId())
                    .issueDescription(saved.getIssueDescription())
                    .appointmentTime(saved.getAppointmentTime())
                    .estimatedCost(saved.getEstimatedCost())
                    .status(saved.getStatus())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "booking.created", event);
        } catch (Exception ex) {
            log.warn("Could not dispatch BookingCreatedEvent to RabbitMQ (continuing): {}", ex.getMessage());
        }
        return saved;
    }

    public Booking getBookingById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for ID: " + id));
    }

    public List<Booking> getBookingsByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public List<Booking> getBookingsByTechnicianId(String technicianId) {
        return repository.findByTechnicianId(technicianId);
    }

    public Booking updateStatus(String id, String newStatus, String cancellationReason) {
        Booking booking = getBookingById(id);
        booking.setStatus(newStatus.toUpperCase());
        if ("CANCELLED".equalsIgnoreCase(newStatus)) {
            booking.setCancellationReason(cancellationReason);
        }
        booking.setUpdatedAt(LocalDateTime.now());
        Booking updated = repository.save(booking);

        switch (updated.getStatus()) {
            case "CONFIRMED":
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "booking.confirmed",
                        BookingConfirmedEvent.builder()
                                .bookingId(updated.getId())
                                .userId(updated.getUserId())
                                .technicianId(updated.getTechnicianId())
                                .appointmentTime(updated.getAppointmentTime())
                                .build());
                break;
            case "CANCELLED":
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "booking.cancelled",
                        BookingCancelledEvent.builder()
                                .bookingId(updated.getId())
                                .userId(updated.getUserId())
                                .technicianId(updated.getTechnicianId())
                                .reason(cancellationReason)
                                .build());
                break;
            case "COMPLETED":
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "booking.completed",
                        BookingCompletedEvent.builder()
                                .bookingId(updated.getId())
                                .userId(updated.getUserId())
                                .technicianId(updated.getTechnicianId())
                                .deviceId(updated.getDeviceId())
                                .build());
                break;
        }

        return updated;
    }

    public Booking reschedule(String id, LocalDateTime newTime) {
        Booking booking = getBookingById(id);
        booking.setAppointmentTime(newTime);
        booking.setUpdatedAt(LocalDateTime.now());
        return repository.save(booking);
    }

    public void deleteBooking(String id) {
        repository.deleteById(id);
    }
}
