package com.spiceroad.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {

    @Test
    void parsesCaseInsensitively() {
        assertEquals(OrderStatus.READY, OrderStatus.parse(" ready "));
    }

    @Test
    void rejectsUnknownOrMissingStatus() {
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.parse("DONE"));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.parse(null));
    }

    @Test
    void allowsMovesUntilFinal() {
        assertTrue(OrderStatus.NEW.canMoveTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.READY.canMoveTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.PREPARING.canMoveTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.NEW.canMoveTo(OrderStatus.NEW));
        assertFalse(OrderStatus.COMPLETED.canMoveTo(OrderStatus.NEW));
        assertFalse(OrderStatus.CANCELLED.canMoveTo(OrderStatus.READY));
    }
}
