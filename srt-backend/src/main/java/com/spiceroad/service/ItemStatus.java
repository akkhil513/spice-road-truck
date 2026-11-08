package com.spiceroad.service;

import java.util.Locale;

/** Kitchen progress of one order line: tapped once = TICKED, tapped again = CLEARED. */
public enum ItemStatus {
    PENDING, TICKED, CLEARED;

    public static ItemStatus parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Item status is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown item status: " + value);
        }
    }

    /** Orders saved before item statuses existed have none stored; they count as PENDING. */
    public static ItemStatus ofStored(String stored) {
        return stored == null || stored.isBlank() ? PENDING : parse(stored);
    }

    /** One step forward (tick, clear) or one step back (recall); skipping a step is never allowed. */
    public boolean canMoveTo(ItemStatus next) {
        return Math.abs(next.ordinal() - ordinal()) == 1;
    }
}
