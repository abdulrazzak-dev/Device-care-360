package com.devicecare360.shared.model;

/**
 * Standard technician verification status enum.
 * Supports legacy 'VERIFIED' status as equivalent to 'APPROVED'.
 */
public enum VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED;

    public static boolean isApproved(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String s = status.trim().toUpperCase();
        return "APPROVED".equals(s) || "VERIFIED".equals(s);
    }

    public static VerificationStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return PENDING;
        }
        String s = status.trim().toUpperCase();
        if ("VERIFIED".equals(s)) {
            return APPROVED;
        }
        try {
            return VerificationStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
