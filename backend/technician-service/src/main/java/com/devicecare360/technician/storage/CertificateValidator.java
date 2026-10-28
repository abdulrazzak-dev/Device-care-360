package com.devicecare360.technician.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class CertificateValidator {

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".jpg", ".jpeg", ".png");
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Magic bytes
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided or file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds 5 MB limit. Provided: " + (file.getSize() / (1024 * 1024)) + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String cleanFilename = sanitizeFilename(originalFilename);
        String extension = getFileExtension(cleanFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid file extension: " + extension + ". Allowed: PDF, JPG, JPEG, PNG");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid file MIME type: " + contentType + ". Allowed: application/pdf, image/jpeg, image/png");
        }

        // Validate actual file signature (magic bytes)
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new IllegalArgumentException("Corrupted or empty file header");
            }

            boolean isPdf = matchesPrefix(header, PDF_MAGIC);
            boolean isJpeg = matchesPrefix(header, JPEG_MAGIC);
            boolean isPng = matchesPrefix(header, PNG_MAGIC);

            if (!isPdf && !isJpeg && !isPng) {
                throw new IllegalArgumentException("File content header does not match a valid PDF, JPEG, or PNG format");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file for validation: " + e.getMessage());
        }
    }

    public String sanitizeFilename(String filename) {
        if (filename == null) return "document";
        String baseName = Path.of(filename).getFileName().toString();
        // Prevent path traversal and remove illegal characters
        return baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex);
    }

    private boolean matchesPrefix(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
