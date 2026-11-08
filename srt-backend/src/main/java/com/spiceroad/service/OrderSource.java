package com.spiceroad.service;

import java.util.Locale;

/** Where an order came from. The number prefix shows the source on every screen. */
public enum OrderSource {
    KIOSK("SRT"), COUNTER("SRT"), ONLINE("SRT"), DOORDASH("DD"), UBEREATS("UE");

    private final String prefix;

    OrderSource(String prefix) {
        this.prefix = prefix;
    }

    /** Display number for the n-th order of the business day, e.g. SRT-001, UE-006. */
    public String formatNumber(long count) {
        return String.format(Locale.ROOT, "%s-%03d", prefix, count);
    }

    public static OrderSource parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Order source is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown order source: " + value);
        }
    }
}
