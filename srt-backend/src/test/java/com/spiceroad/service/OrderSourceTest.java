package com.spiceroad.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderSourceTest {

    @Test
    void ownChannelsShareTheSrtPrefix() {
        assertEquals("SRT-001", OrderSource.COUNTER.formatNumber(1));
        assertEquals("SRT-005", OrderSource.ONLINE.formatNumber(5));
        assertEquals("SRT-009", OrderSource.KIOSK.formatNumber(9));
    }

    @Test
    void thirdPartiesUseTheirOwnPrefixOnTheSharedCount() {
        assertEquals("UE-006", OrderSource.UBEREATS.formatNumber(6));
        assertEquals("DD-007", OrderSource.DOORDASH.formatNumber(7));
    }

    @Test
    void numbersGrowPastThreeDigits() {
        assertEquals("SRT-1000", OrderSource.COUNTER.formatNumber(1000));
    }

    @Test
    void parsesCaseInsensitively() {
        assertEquals(OrderSource.UBEREATS, OrderSource.parse(" ubereats "));
    }

    @Test
    void rejectsUnknownOrMissingSource() {
        assertThrows(IllegalArgumentException.class, () -> OrderSource.parse("FAX"));
        assertThrows(IllegalArgumentException.class, () -> OrderSource.parse(null));
        assertThrows(IllegalArgumentException.class, () -> OrderType.parse("DELIVERY"));
        assertEquals(OrderType.TO_GO, OrderType.parse("to_go"));
    }
}
