package com.devicecare360.notification.messaging;

import com.devicecare360.notification.document.NotificationRecord;
import com.devicecare360.notification.repository.NotificationRecordRepository;
import com.devicecare360.shared.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RabbitListener(queues = "notification.events.queue")
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationRecordRepository repository;

    @RabbitHandler
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent: {}", event);
        saveNotification(event.getUserId(), "Booking Created", "Your appointment is scheduled for " + event.getAppointmentTime(), "BOOKING");
    }

    @RabbitHandler
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent: {}", event);
        saveNotification(event.getUserId(), "Booking Confirmed", "Your booking #" + event.getBookingId() + " has been confirmed.", "BOOKING");
    }

    @RabbitHandler
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent: {}", event);
        saveNotification(event.getUserId(), "Booking Cancelled", "Booking #" + event.getBookingId() + " was cancelled. Reason: " + event.getReason(), "BOOKING");
    }

    @RabbitHandler
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent: {}", event);
        saveNotification(event.getUserId(), "Payment Received", "Payment of $" + event.getAmount() + " completed successfully. Ref: " + event.getTransactionReference(), "PAYMENT");
    }

    @RabbitHandler
    public void handleHighRiskDetected(HighRiskIssueDetectedEvent event) {
        log.info("Received HighRiskIssueDetectedEvent: {}", event);
        saveNotification(event.getUserId(), "CRITICAL SAFETY ALERT", "High risk hazard (" + event.getHazardCategory() + ") detected. " + event.getRecommendedAction(), "SAFETY_ALERT");
    }

    private void saveNotification(String userId, String title, String message, String type) {
        NotificationRecord record = NotificationRecord.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .channel("IN_APP")
                .type(type)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(record);
    }
}
