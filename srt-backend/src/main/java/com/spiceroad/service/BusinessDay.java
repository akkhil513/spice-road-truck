package com.spiceroad.service;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * The truck's business day does not end at midnight. Orders placed before {@code startHour}
 * (local time) still belong to the previous day, so numbering and reports restart in the morning.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    public static void requireValidStartHour(int startHour) {
        if (startHour < 0 || startHour > 23) {
            throw new IllegalArgumentException("Business day start hour must be between 0 and 23");
        }
    }

    public static LocalDate of(ZonedDateTime now, int startHour) {
        requireValidStartHour(startHour);
        LocalDate date = now.toLocalDate();
        return now.getHour() < startHour ? date.minusDays(1) : date;
    }
}
