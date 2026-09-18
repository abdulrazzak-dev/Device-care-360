package com.devicecare360.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class JwtUtils {

    public static final String DEFAULT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    public static final long JWT_EXPIRATION_MS = 86400000L; // 24 hours in milliseconds

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
        Key key = getSigningKey(secret);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token, String secret) {
        try {
            extractAllClaims(token, secret);
            return !isTokenExpired(token, secret);
        } catch (Exception e) {
            log.error("Invalid JWT Token: {}", e.getMessage());
            return false;
        }
    }

    public static boolean isTokenExpired(String token, String secret) {
        return extractExpiration(token, secret).before(new Date());
    }

    public static Date extractExpiration(String token, String secret) {
        return extractClaim(token, secret, Claims::getExpiration);
    }

    public static Key getSigningKey(String secret) {
        String effectiveSecret = (secret != null && !secret.isBlank()) ? secret : DEFAULT_SECRET;
        byte[] keyBytes = effectiveSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
