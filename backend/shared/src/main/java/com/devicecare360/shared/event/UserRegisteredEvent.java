package com.devicecare360.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
