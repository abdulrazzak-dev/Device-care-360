package com.devicecare360.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class JwtUtils {

    public static final String SIGNATURE_ALGORITHM = "HS256";
    public static final long JWT_EXPIRATION_MS = 86400000L; // 24 hours in milliseconds
    private static final int MIN_KEY_BYTES = 32; // 256 bits for HS256

    /**
     * Sanitizes and normalizes the secret string:
     * - Trims leading and trailing whitespace / newlines
     * - Removes surrounding double or single quotation marks
     * - Enforces non-null, non-blank, and minimum key length requirements
     */
    public static String sanitizeSecret(String secret) {
        if (secret == null) {
            throw new IllegalArgumentException("JWT Secret cannot be null. Please configure JWT_SECRET environment variable.");
        }
        String cleaned = secret.trim();
        // Strip surrounding quotes if present (e.g. from environment variable injection)
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'") && cleaned.length() >= 2)) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("JWT Secret cannot be blank. Please configure JWT_SECRET environment variable.");
        }
        byte[] bytes = cleaned.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_KEY_BYTES) {
            throw new IllegalArgumentException("JWT Secret must be at least " + MIN_KEY_BYTES + " bytes (" + (MIN_KEY_BYTES * 8) + " bits) for HS256. Provided length: " + bytes.length + " bytes.");
        }
        return cleaned;
    }

    /**
     * Creates a cryptographic HMAC-SHA Key using canonical UTF-8 bytes from the normalized secret.
     */
    public static Key getSigningKey(String secret) {
        String cleanSecret = sanitizeSecret(secret);
        byte[] keyBytes = cleanSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Computes a safe SHA-256 fingerprint of the normalized secret for logging and diagnostic comparison.
     * Never logs or exposes the raw secret.
     */
    public static String getSecretFingerprint(String secret) {
        try {
            String cleanSecret = sanitizeSecret(secret);
            byte[] keyBytes = cleanSecret.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(keyBytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 message digest algorithm not available", e);
        }
    }

    public static String generateToken(String username, String role, String userId, String secret) {
        return generateToken(username, role, userId, secret, JWT_EXPIRATION_MS);
    }

    public static String generateToken(String username, String role, String userId, String secret, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        return createToken(claims, username, secret, expirationMs);
    }

    private static String createToken(Map<String, Object> claims, String subject, String secret, long expirationMs) {
        Key key = getSigningKey(secret);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extractUsername(String token, String secret) {
        return extractClaim(token, secret, Claims::getSubject);
    }

    public static String extractRole(String token, String secret) {
        Claims claims = extractAllClaims(token, secret);
        return claims.get("role", String.class);
    }

    public static String extractUserId(String token, String secret) {
        Claims claims = extractAllClaims(token, secret);
        return claims.get("userId", String.class);
    }

    public static <T> T extractClaim(String token, String secret, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secret);
        return claimsResolver.apply(claims);
    }

    public static Claims extractAllClaims(String token, String secret) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token cannot be null or empty");
        }
        String cleanToken = token.trim();
        Key key = getSigningKey(secret);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(cleanToken)
                .getBody();
    }

    public static boolean validateToken(String token, String secret) {
        try {
            extractAllClaims(token, secret);
            return !isTokenExpired(token, secret);
        } catch (Exception e) {
            log.warn("Invalid JWT Token validation failure: {}", e.getMessage());
            return false;
        }
    }

    public static boolean isTokenExpired(String token, String secret) {
        return extractExpiration(token, secret).before(new Date());
    }

    public static Date extractExpiration(String token, String secret) {
        return extractClaim(token, secret, Claims::getExpiration);
    }
}
