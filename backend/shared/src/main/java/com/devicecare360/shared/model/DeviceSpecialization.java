package com.devicecare360.shared.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Canonical device specialization categories used across all services and frontend.
 */
public enum DeviceSpecialization {
    SMARTPHONE_MOBILE("Smartphone / Mobile"),
    LAPTOP_DESKTOP_PC("Laptop / Desktop PC"),
    REFRIGERATOR("Refrigerator"),
    AIR_CONDITIONER("Air Conditioner (AC)"),
    SMART_TV_AUDIO("Smart TV / Audio"),
    WASHING_MACHINE("Washing Machine"),
    MICROWAVE_KITCHEN_APPLIANCES("Microwave / Kitchen Appliances");

    private final String displayLabel;

    DeviceSpecialization(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    /**
     * Resolves various alias strings, display labels, or legacy names into the canonical enum name.
     * Returns null if unmapped.
     */
    public static String normalizeToCanonical(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String raw = input.trim().toUpperCase(Locale.ROOT);

        // Exact match with enum name
        for (DeviceSpecialization spec : values()) {
            if (spec.name().equals(raw)) {
                return spec.name();
            }
        }

        // Match common variations and keywords
        if (raw.contains("SMARTPHONE") || raw.contains("MOBILE") || raw.contains("PHONE")) {
            return SMARTPHONE_MOBILE.name();
        }
        if (raw.contains("LAPTOP") || raw.contains("DESKTOP") || raw.contains("PC") || raw.contains("COMPUTER")) {
            return LAPTOP_DESKTOP_PC.name();
        }
        if (raw.contains("REFRIGERATOR") || raw.contains("FRIDGE")) {
            return REFRIGERATOR.name();
        }
        if (raw.contains("AIR CONDITIONER") || raw.contains("AC") || raw.contains("A/C") || raw.contains("AIR_CONDITIONER")) {
            return AIR_CONDITIONER.name();
        }
        if (raw.contains("TELEVISION") || raw.contains("TV") || raw.contains("AUDIO") || raw.contains("SOUND")) {
            return SMART_TV_AUDIO.name();
        }
        if (raw.contains("WASHING") || raw.contains("WASHER") || raw.contains("LAUNDRY")) {
            return WASHING_MACHINE.name();
        }
        if (raw.contains("MICROWAVE") || raw.contains("KITCHEN") || raw.contains("OVEN")) {
            return MICROWAVE_KITCHEN_APPLIANCES.name();
        }

        return null;
    }

    /**
     * Checks whether a technician's list of specializations matches the target category (supports canonical or legacy formats).
     */
    public static boolean matchesCategory(List<String> technicianSpecializations, String targetCategory) {
        if (technicianSpecializations == null || technicianSpecializations.isEmpty() || targetCategory == null) {
            return false;
        }

        String targetCanonical = normalizeToCanonical(targetCategory);
        if (targetCanonical == null) {
            targetCanonical = targetCategory.trim().toUpperCase(Locale.ROOT);
        }

        for (String spec : technicianSpecializations) {
            if (spec == null) continue;
            String specCanonical = normalizeToCanonical(spec);
            if (specCanonical != null && specCanonical.equalsIgnoreCase(targetCanonical)) {
                return true;
            }
            if (spec.equalsIgnoreCase(targetCategory) || spec.equalsIgnoreCase(targetCanonical)) {
                return true;
            }
        }
        return false;
    }
}
