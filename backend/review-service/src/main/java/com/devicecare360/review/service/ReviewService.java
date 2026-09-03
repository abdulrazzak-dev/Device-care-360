package com.devicecare360.review.service;

import com.devicecare360.review.client.BookingClient;
import com.devicecare360.review.config.RabbitMQConfig;
import com.devicecare360.review.document.Review;
import com.devicecare360.review.repository.ReviewRepository;
import com.devicecare360.shared.event.ReviewCreatedEvent;
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
public class ReviewService {

    private final ReviewRepository repository;
    private final BookingClient bookingClient;
    private final RabbitTemplate rabbitTemplate;

    public Review createReview(String userId, Review review) {
        if (repository.existsByBookingId(review.getBookingId())) {
            throw new IllegalArgumentException("A review already exists for booking ID: " + review.getBookingId());
        }

        var bookingResp = bookingClient.getBookingById(review.getBookingId());
        if (bookingResp == null || bookingResp.getData() == null) {
            throw new IllegalArgumentException("Booking not found: " + review.getBookingId());
        }

        Map<String, Object> bookingData = bookingResp.getData();
        String bookingUserId = (String) bookingData.get("userId");
        String bookingTechId = (String) bookingData.get("technicianId");
        String status = (String) bookingData.get("status");

        if (!"COMPLETED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Reviews are allowed only for COMPLETED bookings");
        }

        if (bookingUserId != null && !bookingUserId.equals(userId)) {
            throw new IllegalArgumentException("You can only review your own completed booking");
        }

        review.setUserId(userId);
        review.setTechnicianId(bookingTechId);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = repository.save(review);

        ReviewCreatedEvent event = ReviewCreatedEvent.builder()
                .reviewId(saved.getId())
                .bookingId(saved.getBookingId())
                .userId(saved.getUserId())
                .technicianId(saved.getTechnicianId())
                .rating(saved.getRating())
                .comment(saved.getComment())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "review.created", event);
        return saved;
    }

    public List<Review> getReviewsByTechnician(String technicianId) {
        return repository.findByTechnicianId(technicianId);
    }

    public List<Review> getReviewsByUser(String userId) {
        return repository.findByUserId(userId);
    }
}
