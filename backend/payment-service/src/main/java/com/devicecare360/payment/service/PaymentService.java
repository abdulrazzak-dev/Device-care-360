package com.devicecare360.payment.service;

import com.devicecare360.payment.config.RabbitMQConfig;
import com.devicecare360.payment.document.PaymentRecord;
import com.devicecare360.payment.provider.PaymentProvider;
import com.devicecare360.payment.repository.PaymentRecordRepository;
import com.devicecare360.shared.event.PaymentCompletedEvent;
import com.devicecare360.shared.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRecordRepository repository;
    private final PaymentProvider paymentProvider;
    private final RabbitTemplate rabbitTemplate;

    public PaymentRecord processPayment(PaymentRecord record) {
        record.setStatus("PROCESSING");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        PaymentRecord saved = repository.save(record);

        PaymentProvider.PaymentResult result = paymentProvider.processPayment(saved);

        if (result.success()) {
            saved.setStatus("PAID");
            saved.setTransactionReference(result.transactionRef());
            saved.setReceiptUrl(result.receiptUrl());
            saved.setUpdatedAt(LocalDateTime.now());
            PaymentRecord completed = repository.save(saved);

            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .paymentId(completed.getId())
                    .bookingId(completed.getBookingId())
                    .userId(completed.getUserId())
                    .amount(completed.getAmount())
                    .paymentProvider(completed.getPaymentProvider())
                    .transactionReference(completed.getTransactionReference())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "payment.completed", event);
            return completed;
        } else {
            saved.setStatus("FAILED");
            saved.setFailureReason(result.errorMessage());
            saved.setUpdatedAt(LocalDateTime.now());
            PaymentRecord failed = repository.save(saved);

            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .paymentId(failed.getId())
                    .bookingId(failed.getBookingId())
                    .userId(failed.getUserId())
                    .amount(failed.getAmount())
                    .reason(failed.getFailureReason())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "payment.failed", event);
            return failed;
        }
    }

    public PaymentRecord getPaymentById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for ID: " + id));
    }

    public List<PaymentRecord> getPaymentsByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public PaymentRecord getPaymentByBookingId(String bookingId) {
        return repository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for Booking ID: " + bookingId));
    }
}
