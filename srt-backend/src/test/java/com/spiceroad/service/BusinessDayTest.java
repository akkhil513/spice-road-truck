package com.spiceroad.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessDayTest {

    private static final ZoneId NY = ZoneId.of("America/New_York");

    private static ZonedDateTime at(int day, int hour, int minute) {
        return ZonedDateTime.of(2026, 11, day, hour, minute, 0, 0, NY);
    }

    @Test
    void ordersBeforeStartHourBelongToPreviousDay() {
        assertEquals(LocalDate.of(2026, 11, 6), BusinessDay.of(at(7, 0, 0), 4));
        assertEquals(LocalDate.of(2026, 11, 6), BusinessDay.of(at(7, 3, 59), 4));
    }

    @Test
    void ordersFromStartHourBelongToSameDay() {
        assertEquals(LocalDate.of(2026, 11, 7), BusinessDay.of(at(7, 4, 0), 4));
        assertEquals(LocalDate.of(2026, 11, 7), BusinessDay.of(at(7, 23, 59), 4));
    }

    @Test
    void midnightStartBehavesLikeCalendarDay() {
        assertEquals(LocalDate.of(2026, 11, 7), BusinessDay.of(at(7, 0, 0), 0));
    }

    @Test
    void crossesMonthBoundary() {
        ZonedDateTime firstOfDecemberAt2am = ZonedDateTime.of(2026, 12, 1, 2, 0, 0, 0, NY);
        assertEquals(LocalDate.of(2026, 11, 30), BusinessDay.of(firstOfDecemberAt2am, 4));
    }

    @Test
    void rejectsInvalidStartHour() {
        assertThrows(IllegalArgumentException.class, () -> BusinessDay.of(at(7, 12, 0), -1));
        assertThrows(IllegalArgumentException.class, () -> BusinessDay.of(at(7, 12, 0), 24));
        assertThrows(IllegalArgumentException.class, () -> BusinessDay.requireValidStartHour(99));
    }
}
