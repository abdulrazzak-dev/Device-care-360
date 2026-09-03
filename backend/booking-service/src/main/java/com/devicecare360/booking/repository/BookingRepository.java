package com.devicecare360.booking.repository;

import com.devicecare360.booking.document.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByTechnicianId(String technicianId);
}
