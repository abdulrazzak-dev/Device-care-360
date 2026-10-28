package com.devicecare360.technician.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateUploadResponse {
    private String certificateUrl;
    private String certificateFilename;
    private LocalDateTime uploadedAt;
    private String verificationStatus;
    private String message;
}
