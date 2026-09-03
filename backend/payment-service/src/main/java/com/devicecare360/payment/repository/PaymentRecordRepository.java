package com.devicecare360.payment.repository;

import com.devicecare360.payment.document.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {
    List<PaymentRecord> findByUserId(String userId);
    Optional<PaymentRecord> findByBookingId(String bookingId);
}
