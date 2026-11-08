package com.spiceroad.service;

/**
 * Order lifecycle. HELD is a counter order parked before it is sent to the kitchen. COMPLETED
 * (picked up) and CANCELLED are final.
 */
public enum OrderStatus {
    NEW, HELD, PREPARING, READY, COMPLETED, CANCELLED;

    public static OrderStatus parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status is required");
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + value);
        }
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /** Any move is allowed (including stepping back to recall an order) until the order is final. */
    public boolean canMoveTo(OrderStatus next) {
        return this != next && !isFinal();
    }
}
