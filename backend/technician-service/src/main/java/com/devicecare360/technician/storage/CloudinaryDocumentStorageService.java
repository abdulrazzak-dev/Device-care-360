package com.devicecare360.technician.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CloudinaryDocumentStorageService implements DocumentStorageService {

    @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloudName;

    @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}")
    private String apiKey;

    @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}")
    private String apiSecret;

    @Value("${cloudinary.url:${CLOUDINARY_URL:}}")
    private String cloudinaryUrl;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);

        // Attempt to parse CLOUDINARY_URL if individual properties are missing
        if ((cloudName == null || cloudName.isBlank()) && cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            parseCloudinaryUrl(cloudinaryUrl);
        }

        if (isConfigured()) {
            log.info("Cloudinary storage provider initialized successfully for cloud: {}", cloudName);
        } else {
            log.warn("Cloudinary storage provider is NOT configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.");
        }
    }

    private void parseCloudinaryUrl(String url) {
        try {
            // Format: cloudinary://api_key:api_secret@cloud_name
            Pattern pattern = Pattern.compile("^cloudinary://([^:]+):([^@]+)@(.+)$");
            Matcher matcher = pattern.matcher(url.trim());
            if (matcher.matches()) {
                this.apiKey = matcher.group(1);
                this.apiSecret = matcher.group(2);
                this.cloudName = matcher.group(3);
                log.info("Parsed Cloudinary credentials from CLOUDINARY_URL for cloud: {}", this.cloudName);
            }
        } catch (Exception e) {
            log.warn("Failed to parse CLOUDINARY_URL: {}", e.getMessage());
        }
    }

    @Override
    public boolean isConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    @Override
    public StoredDocument uploadDocument(MultipartFile file, String userId, String documentType) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Cloudinary storage provider is not configured. Please configure CLOUDINARY_CLOUD_NAME, " +
                    "CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET (or CLOUDINARY_URL) in environment variables."
            );
        }

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String folder = "devicecare360/technicians/certificates";
        String cleanUserId = userId != null ? userId.replaceAll("[^a-zA-Z0-9_-]", "") : "user";
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String publicId = "cert_" + cleanUserId + "_" + uniqueId;

        // Signature string in alphabetical order of parameters
        // folder=...&public_id=...&timestamp=...<apiSecret>
        String toSign = "folder=" + folder + "&public_id=" + publicId + "&timestamp=" + timestamp + apiSecret;
        String signature = generateSha1Hex(toSign);

        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName.trim() + "/auto/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
            }
        };

        body.add("file", fileResource);
        body.add("api_key", apiKey.trim());
        body.add("timestamp", timestamp);
        body.add("folder", folder);
        body.add("public_id", publicId);
        body.add("signature", signature);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IOException("Cloudinary upload returned status: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String secureUrl = root.hasNonNull("secure_url") ? root.get("secure_url").asText() : null;
            if (secureUrl == null) {
                secureUrl = root.hasNonNull("url") ? root.get("url").asText() : null;
            }

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new IOException("Failed to obtain secure_url from Cloudinary response: " + response.getBody());
            }

            log.info("Certificate successfully stored in Cloudinary for user: {}, publicId: {}", userId, publicId);

            return StoredDocument.builder()
                    .url(secureUrl)
                    .publicId(publicId)
                    .filename(file.getOriginalFilename())
                    .sizeBytes(file.getSize())
                    .mimeType(file.getContentType())
                    .build();

        } catch (Exception e) {
            log.error("Cloudinary upload failed for technician {}: {}", userId, e.getMessage());
            throw new IOException("Upload provider error: " + e.getMessage(), e);
        }
    }

    private String generateSha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }
}
