package com.devicecare360.user.service;

import com.devicecare360.user.document.UserProfile;
import com.devicecare360.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile createUserProfile(UserProfile profile) {
        if (profile.getId() != null && userProfileRepository.existsById(profile.getId())) {
            log.info("UserProfile already exists for ID: {}. Returning existing record.", profile.getId());
            return userProfileRepository.findById(profile.getId()).get();
        }
        if (profile.getStatus() == null) {
            profile.setStatus("ACTIVE");
        }
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        UserProfile saved = userProfileRepository.save(profile);
        log.info("UserProfile successfully persisted for ID: {}", saved.getId());
        return saved;
    }

    public UserProfile getUserProfile(String id) {
        return userProfileRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("UserProfile not found for ID: {}. Lazily initializing default active profile.", id);
                    UserProfile fallback = UserProfile.builder()
                            .id(id)
                            .status("ACTIVE")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return userProfileRepository.save(fallback);
                });
    }

    public UserProfile updateUserProfile(String id, UserProfile updateRequest) {
        UserProfile existing = getUserProfile(id);
        if (updateRequest.getFullName() != null) existing.setFullName(updateRequest.getFullName());
        if (updateRequest.getPhone() != null) existing.setPhone(updateRequest.getPhone());
        if (updateRequest.getAddress() != null) existing.setAddress(updateRequest.getAddress());
        if (updateRequest.getPreferredLanguage() != null) existing.setPreferredLanguage(updateRequest.getPreferredLanguage());
        existing.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(existing);
    }

    public void deleteUserProfile(String id) {
        UserProfile existing = getUserProfile(id);
        existing.setStatus("DELETED");
        existing.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(existing);
    }
}
