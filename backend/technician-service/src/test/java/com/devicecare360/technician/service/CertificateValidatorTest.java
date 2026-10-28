package com.devicecare360.technician.service;

import com.devicecare360.technician.storage.CertificateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class CertificateValidatorTest {

    private CertificateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CertificateValidator();
    }

    @Test
    @DisplayName("Valid PDF with %PDF magic header passes validation")
    void testValidPdf() {
        byte[] pdfContent = "%PDF-1.4 sample content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "certificate.pdf", "application/pdf", pdfContent);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    @DisplayName("Valid PNG with PNG magic header passes validation")
    void testValidPng() {
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "license.png", "image/png", pngHeader);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    @DisplayName("File exceeding 5MB is rejected")
    void testOversizedFile() {
        byte[] oversized = new byte[(int) (CertificateValidator.MAX_FILE_SIZE_BYTES + 1024)];
        System.arraycopy("%PDF".getBytes(), 0, oversized, 0, 4);
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", oversized);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(file));
        assertTrue(ex.getMessage().contains("exceeds 5 MB limit"));
    }

    @Test
    @DisplayName("Invalid file extension is rejected")
    void testInvalidExtension() {
        byte[] content = "dummy".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/octet-stream", content);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(file));
        assertTrue(ex.getMessage().contains("Invalid file extension"));
    }

    @Test
    @DisplayName("Spoofed file with PDF extension but corrupted header is rejected")
    void testSpoofedMimeType() {
        byte[] fakeContent = "This is not a real PDF file".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf", fakeContent);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(file));
        assertTrue(ex.getMessage().contains("does not match a valid PDF"));
    }
}
