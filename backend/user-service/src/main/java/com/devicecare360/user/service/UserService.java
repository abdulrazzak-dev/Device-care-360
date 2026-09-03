package com.devicecare360.user.service;

import com.devicecare360.user.document.UserProfile;
import com.devicecare360.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile getUserProfile(String id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for ID: " + id));
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
