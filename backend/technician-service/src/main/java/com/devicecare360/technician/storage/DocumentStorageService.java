package com.devicecare360.technician.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentStorageService {
    StoredDocument uploadDocument(MultipartFile file, String userId, String documentType) throws IOException;
    boolean isConfigured();
}
