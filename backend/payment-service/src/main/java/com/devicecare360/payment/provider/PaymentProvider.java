package com.devicecare360.payment.provider;

import com.devicecare360.payment.document.PaymentRecord;

public interface PaymentProvider {
    String getProviderName();
    PaymentResult processPayment(PaymentRecord record);
    PaymentResult refundPayment(PaymentRecord record);

    record PaymentResult(boolean success, String transactionRef, String receiptUrl, String errorMessage) {}
}
