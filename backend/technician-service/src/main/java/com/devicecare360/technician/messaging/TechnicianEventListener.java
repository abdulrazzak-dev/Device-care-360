package com.devicecare360.technician.messaging;

import com.devicecare360.shared.event.ReviewCreatedEvent;
import com.devicecare360.shared.event.TechnicianRegisteredEvent;
import com.devicecare360.technician.config.RabbitMQConfig;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TechnicianEventListener {

    private final TechnicianProfileRepository repository;

    @RabbitListener(queues = RabbitMQConfig.TECH_REGISTERED_QUEUE)
    public void handleTechnicianRegistered(TechnicianRegisteredEvent event) {
        log.info("Received TechnicianRegisteredEvent: {}", event);
        if (!repository.existsById(event.getTechnicianId())) {
            TechnicianProfile profile = TechnicianProfile.builder()
                    .id(event.getTechnicianId())
                    .fullName(event.getFullName())
                    .email(event.getEmail())
                    .phone(event.getPhone())
                    .specializations(event.getSpecializations())
                    .serviceAreas(event.getServiceAreas())
                    .available(true)
                    .verificationStatus("PENDING")
                    .averageRating(0.0)
                    .totalReviews(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            repository.save(profile);
            log.info("TechnicianProfile saved for ID: {}", event.getTechnicianId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.REVIEW_CREATED_QUEUE)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        log.info("Received ReviewCreatedEvent for technician: {}", event.getTechnicianId());
        repository.findById(event.getTechnicianId()).ifPresent(tech -> {
            int oldTotal = tech.getTotalReviews();
            double oldAvg = tech.getAverageRating();
            int newTotal = oldTotal + 1;
            double newAvg = ((oldAvg * oldTotal) + event.getRating()) / newTotal;

            tech.setTotalReviews(newTotal);
            tech.setAverageRating(Math.round(newAvg * 10.0) / 10.0);
            tech.setUpdatedAt(LocalDateTime.now());
            repository.save(tech);
            log.info("Updated technician rating summary: avg={}, count={}", tech.getAverageRating(), tech.getTotalReviews());
        });
    }
}
