package com.devicecare360.review.repository;

import com.devicecare360.review.document.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTechnicianId(String technicianId);
    List<Review> findByUserId(String userId);
    Optional<Review> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
}
