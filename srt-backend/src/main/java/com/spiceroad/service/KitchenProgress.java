package com.spiceroad.service;

import java.util.List;

/** Order status that follows from the state of its lines while the kitchen is working on it. */
public final class KitchenProgress {

    private KitchenProgress() {
    }

    /** All lines cleared = READY, nothing touched yet = NEW, anything in between = PREPARING. */
    public static OrderStatus orderStatusFor(List<ItemStatus> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item");
        }
        if (items.stream().allMatch(s -> s == ItemStatus.CLEARED)) {
            return OrderStatus.READY;
        }
        if (items.stream().allMatch(s -> s == ItemStatus.PENDING)) {
            return OrderStatus.NEW;
        }
        return OrderStatus.PREPARING;
    }
}
