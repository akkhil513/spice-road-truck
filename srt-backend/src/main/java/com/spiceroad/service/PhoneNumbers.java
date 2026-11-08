package com.spiceroad.service;

import jakarta.ws.rs.BadRequestException;

import java.util.regex.Pattern;

/** Normalizes customer phone numbers to E.164 (+14045550142) so they work as lookup keys. */
public final class PhoneNumbers {

    private static final int MAX_INPUT_LENGTH = 32;
    private static final Pattern SEPARATORS = Pattern.compile("[\\s().\\-]");
    private static final Pattern E164 = Pattern.compile("\\+[1-9]\\d{7,14}");
    private static final Pattern US_10 = Pattern.compile("[2-9]\\d{9}");
    private static final Pattern US_11 = Pattern.compile("1[2-9]\\d{9}");

    private PhoneNumbers() {
    }

    /** Returns the E.164 form, or null when no number was given. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_INPUT_LENGTH) {
            throw invalid();
        }
        String compact = SEPARATORS.matcher(trimmed).replaceAll("");
        String candidate;
        if (compact.startsWith("+")) {
            candidate = compact;
        } else if (US_10.matcher(compact).matches()) {
            candidate = "+1" + compact;
        } else if (US_11.matcher(compact).matches()) {
            candidate = "+" + compact;
        } else {
            throw invalid();
        }
        if (!E164.matcher(candidate).matches()) {
            throw invalid();
        }
        return candidate;
    }

    private static BadRequestException invalid() {
        return new BadRequestException("Phone number is not valid");
    }
}
