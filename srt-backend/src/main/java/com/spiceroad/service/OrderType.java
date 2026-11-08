package com.spiceroad.service;

import java.util.Locale;

public enum OrderType {
    DINE_IN, TO_GO;

    public static OrderType parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Order type is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown order type: " + value);
        }
    }
}
