package com.devicecare360.shared.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Test
    @DisplayName("Generate token with default 24 hour expiration and verify claims")
    void testGenerateTokenWithDefaultExpiration() {
        long beforeGen = System.currentTimeMillis();
        String token = JwtUtils.generateToken("johndoe", "USER", "user-id-123", SECRET);
        long afterGen = System.currentTimeMillis();

        assertNotNull(token);
        assertFalse(token.isBlank());

        // Validate token
        assertTrue(JwtUtils.validateToken(token, SECRET));

        // Verify claims
        assertEquals("johndoe", JwtUtils.extractUsername(token, SECRET));
        assertEquals("USER", JwtUtils.extractRole(token, SECRET));
        assertEquals("user-id-123", JwtUtils.extractUserId(token, SECRET));

        Claims claims = JwtUtils.extractAllClaims(token, SECRET);
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        assertNotNull(issuedAt);
        assertNotNull(expiration);

        // Verify issuedAt is around current time
        assertTrue(issuedAt.getTime() >= (beforeGen / 1000) * 1000 - 1000);
        assertTrue(issuedAt.getTime() <= afterGen + 1000);

        // Verify exp - iat is exactly 24 hours (86,400,000 ms)
        long durationMs = expiration.getTime() - issuedAt.getTime();
        assertEquals(JwtUtils.JWT_EXPIRATION_MS, durationMs, "Token duration must equal 24 hours (86400000 ms)");
        assertFalse(JwtUtils.isTokenExpired(token, SECRET));
    }

    @Test
    @DisplayName("Generate token with custom expiration")
    void testGenerateTokenWithCustomExpiration() {
        long customExpMs = 3600000L; // 1 hour
        String token = JwtUtils.generateToken("techuser", "TECHNICIAN", "tech-456", SECRET, customExpMs);

        assertNotNull(token);
        assertTrue(JwtUtils.validateToken(token, SECRET));
        assertEquals("techuser", JwtUtils.extractUsername(token, SECRET));
        assertEquals("TECHNICIAN", JwtUtils.extractRole(token, SECRET));
        assertEquals("tech-456", JwtUtils.extractUserId(token, SECRET));

        Claims claims = JwtUtils.extractAllClaims(token, SECRET);
        long durationMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(customExpMs, durationMs);
    }

    @Test
    @DisplayName("Token validation fails for tampered or invalid secret")
    void testValidationFailsWithWrongSecret() {
        String token = JwtUtils.generateToken("user1", "USER", "uid1", SECRET);
        String wrongSecret = "999E635266556A586E3272357538782F413F4428472B4B6250645367566B5999";

        assertFalse(JwtUtils.validateToken(token, wrongSecret));
    }

    @Test
    @DisplayName("Expired token validation returns false")
    void testExpiredTokenValidation() {
        // Create an already-expired token (-5000 ms)
        String expiredToken = JwtUtils.generateToken("user1", "USER", "uid1", SECRET, -5000L);

        assertFalse(JwtUtils.validateToken(expiredToken, SECRET));
    }
}