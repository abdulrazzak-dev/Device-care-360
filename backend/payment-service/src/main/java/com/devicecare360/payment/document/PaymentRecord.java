package com.devicecare360.payment.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payment_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {
    @Id
    private String id;
    private String bookingId;
    private String userId;
    private BigDecimal amount;
    private String currency; // USD, LKR, EUR, etc.
    private String paymentProvider; // Stripe, PayHere, Mock
    private String transactionReference;
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSING, PAID, FAILED, REFUNDED
    private String receiptUrl;
    private String failureReason;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
