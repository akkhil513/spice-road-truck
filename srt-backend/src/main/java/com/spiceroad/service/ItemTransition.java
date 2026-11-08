package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether one kitchen tap is allowed and what the write must look like. The client says
 * which state it saw ({@code from}) and which it wants ({@code to}); the plan is rejected if the
 * stored item is no longer in {@code from}, so two chef screens cannot skip or repeat a step.
 */
public final class ItemTransition {

    private ItemTransition() {
    }

    /** Everything the repository needs for one conditional write. */
    public record Plan(int index, String lineId, ItemStatus from, ItemStatus to,
                       OrderStatus seenOrderStatus, OrderStatus newOrderStatus, Integer seenRevision) {
    }

    public static Plan plan(SRTOrder order, String lineId, ItemStatus from, ItemStatus to) {
        if (!from.canMoveTo(to)) {
            throw new BadRequestException("An item cannot move from " + from + " to " + to);
        }
        OrderStatus current = OrderStatus.parse(order.getStatus());
        if (current.isFinal()) {
            throw conflict("Order is " + current + " and its items can no longer change");
        }
        if (order.getSentToKitchenAt() == null) {
            throw conflict("Order has not been sent to the kitchen");
        }
        List<OrderItem> items = order.getItems();
        int index = indexOf(items, lineId);
        if (index < 0) {
            throw new NotFoundException("Item not found");
        }
        ItemStatus actual = ItemStatus.ofStored(items.get(index).getItemStatus());
        if (actual != from) {
            throw conflict("Item is " + actual + ", not " + from + ", please refresh");
        }

        List<ItemStatus> after = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            after.add(i == index ? to : ItemStatus.ofStored(items.get(i).getItemStatus()));
        }
        return new Plan(index, lineId, from, to, current,
                KitchenProgress.orderStatusFor(after), order.getKitchenRevision());
    }

    private static int indexOf(List<OrderItem> items, String lineId) {
        if (items == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (lineId.equals(items.get(i).getLineId())) {
                return i;
            }
        }
        return -1;
    }

    private static ClientErrorException conflict(String message) {
        return new ClientErrorException(message, Response.Status.CONFLICT);
    }
}
