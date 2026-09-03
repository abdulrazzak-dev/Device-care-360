package com.devicecare360.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {
    private String paymentId;
    private String bookingId;
    private String userId;
    private BigDecimal amount;
    private String paymentProvider;
    private String transactionReference;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
