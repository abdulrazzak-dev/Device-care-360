package com.devicecare360.booking.service;

import com.devicecare360.booking.client.TechnicianClient;
import com.devicecare360.booking.config.RabbitMQConfig;
import com.devicecare360.booking.document.Booking;
import com.devicecare360.booking.repository.BookingRepository;
import com.devicecare360.shared.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

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
        try {
            var techResp = technicianClient.getTechnicianById(booking.getTechnicianId());
            if (techResp == null || techResp.getData() == null) {
                throw new IllegalArgumentException("Technician not found: " + booking.getTechnicianId());
            }
            Map<String, Object> techData = techResp.getData();
            Boolean available = (Boolean) techData.get("available");
            if (available != null && !available) {
                log.warn("Technician {} is currently not available", booking.getTechnicianId());
            }
        } catch (Exception e) {
            log.error("Failed synchronous technician verification: {}", e.getMessage());
        }

        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking saved = repository.save(booking);

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
