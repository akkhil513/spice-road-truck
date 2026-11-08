package com.spiceroad.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStatusTest {

    @Test
    void allowsOneStepForwardAndOneStepBack() {
        assertTrue(ItemStatus.PENDING.canMoveTo(ItemStatus.TICKED));
        assertTrue(ItemStatus.TICKED.canMoveTo(ItemStatus.CLEARED));
        assertTrue(ItemStatus.CLEARED.canMoveTo(ItemStatus.TICKED));
        assertTrue(ItemStatus.TICKED.canMoveTo(ItemStatus.PENDING));
    }

    @Test
    void neverSkipsAStepOrStaysPut() {
        assertFalse(ItemStatus.PENDING.canMoveTo(ItemStatus.CLEARED));
        assertFalse(ItemStatus.CLEARED.canMoveTo(ItemStatus.PENDING));
        assertFalse(ItemStatus.TICKED.canMoveTo(ItemStatus.TICKED));
    }

    @Test
    void parsesCaseInsensitivelyAndRejectsUnknown() {
        assertEquals(ItemStatus.TICKED, ItemStatus.parse(" ticked "));
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.parse("DONE"));
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.parse(null));
    }

    @Test
    void missingStoredStatusCountsAsPending() {
        assertEquals(ItemStatus.PENDING, ItemStatus.ofStored(null));
        assertEquals(ItemStatus.PENDING, ItemStatus.ofStored(" "));
        assertEquals(ItemStatus.CLEARED, ItemStatus.ofStored("CLEARED"));
    }
}
