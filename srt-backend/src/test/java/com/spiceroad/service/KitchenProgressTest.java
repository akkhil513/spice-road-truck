package com.spiceroad.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.spiceroad.service.ItemStatus.CLEARED;
import static com.spiceroad.service.ItemStatus.PENDING;
import static com.spiceroad.service.ItemStatus.TICKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KitchenProgressTest {

    @Test
    void untouchedOrderIsNew() {
        assertEquals(OrderStatus.NEW, KitchenProgress.orderStatusFor(List.of(PENDING, PENDING)));
    }

    @Test
    void anyProgressMeansPreparing() {
        assertEquals(OrderStatus.PREPARING, KitchenProgress.orderStatusFor(List.of(TICKED, PENDING)));
        assertEquals(OrderStatus.PREPARING, KitchenProgress.orderStatusFor(List.of(CLEARED, PENDING)));
        assertEquals(OrderStatus.PREPARING, KitchenProgress.orderStatusFor(List.of(CLEARED, TICKED)));
    }

    @Test
    void allClearedMeansReady() {
        assertEquals(OrderStatus.READY, KitchenProgress.orderStatusFor(List.of(CLEARED, CLEARED)));
        assertEquals(OrderStatus.READY, KitchenProgress.orderStatusFor(List.of(CLEARED)));
    }

    @Test
    void recallingBringsTheOrderBack() {
        assertEquals(OrderStatus.PREPARING, KitchenProgress.orderStatusFor(List.of(CLEARED, TICKED)));
        assertEquals(OrderStatus.NEW, KitchenProgress.orderStatusFor(List.of(PENDING)));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> KitchenProgress.orderStatusFor(List.of()));
        assertThrows(IllegalArgumentException.class, () -> KitchenProgress.orderStatusFor(null));
    }
}
