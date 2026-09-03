package com.devicecare360.payment.provider;

import com.devicecare360.payment.document.PaymentRecord;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public PaymentResult processPayment(PaymentRecord record) {
        String txRef = "TX-MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String receipt = "https://devicecare360.com/receipts/" + txRef;
        return new PaymentResult(true, txRef, receipt, null);
    }

    @Override
    public PaymentResult refundPayment(PaymentRecord record) {
        String txRef = "REF-MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txRef, null, null);
    }
}
