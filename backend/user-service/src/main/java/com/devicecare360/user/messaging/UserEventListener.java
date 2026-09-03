package com.devicecare360.user.messaging;

import com.devicecare360.shared.event.UserRegisteredEvent;
import com.devicecare360.user.config.RabbitMQConfig;
import com.devicecare360.user.document.UserProfile;
import com.devicecare360.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserProfileRepository userProfileRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent: {}", event);
        if (!userProfileRepository.existsById(event.getUserId())) {
            UserProfile profile = UserProfile.builder()
                    .id(event.getUserId())
                    .username(event.getUsername())
                    .email(event.getEmail())
                    .fullName(event.getFullName())
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userProfileRepository.save(profile);
            log.info("UserProfile created for userId: {}", event.getUserId());
        }
    }
}
