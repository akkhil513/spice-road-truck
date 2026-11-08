package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/**
 * Business rules for counter orders. Every check works on the stored order only, never on
 * anything the client claims, and each one throws 409 when the order is not in a state that
 * allows the action.
 */
public final class CounterOrderPolicy {

    private CounterOrderPolicy() {
    }

    /** Items can be changed only while the order is unsent and not final. */
    public static void requireEditable(SRTOrder order) {
        requireUnsent(order, "Items can only be changed before the order is sent to the kitchen");
        OrderStatus status = OrderStatus.parse(order.getStatus());
        if (status != OrderStatus.NEW && status != OrderStatus.HELD) {
            throw conflict("Items can only be changed while the order is new or held");
        }
        if (!PaymentStatus.UNPAID.name().equals(order.getPaymentStatus())) {
            throw conflict("Items cannot be changed on an order that has payments");
        }
    }

    /** The screen must send the version it saw; a different stored version means someone else edited. */
    public static void requireVersion(SRTOrder order, Integer clientVersion) {
        if (clientVersion == null) {
            throw new BadRequestException("orderVersion is required");
        }
        int stored = order.getOrderVersion() == null ? 0 : order.getOrderVersion();
        if (stored != clientVersion) {
            throw conflict("Order was changed by someone else, please refresh");
        }
    }

    /** Counter screens create COUNTER or KIOSK orders; other sources have their own routes. */
    public static OrderSource parseCounterSource(String value) {
        OrderSource source;
        try {
            source = OrderSource.parse(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (source != OrderSource.COUNTER && source != OrderSource.KIOSK) {
            throw new BadRequestException("Source must be COUNTER or KIOSK");
        }
        return source;
    }

    public static OrderType parseOrderType(String value) {
        try {
            return OrderType.parse(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /** Dine-in needs a table number 1 to 99; to-go must not have one. */
    public static String cleanTableNumber(OrderType type, String value) {
        String trimmed = value == null ? "" : value.trim();
        if (type == OrderType.TO_GO) {
            if (!trimmed.isEmpty()) {
                throw new BadRequestException("A to-go order cannot have a table number");
            }
            return null;
        }
        if (!trimmed.matches("[1-9][0-9]?")) {
            throw new BadRequestException("Dine-in orders need a table number from 1 to 99");
        }
        return trimmed;
    }

    /** Walk-ins may skip the name. */
    public static String cleanCustomerName(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return "Walk-in";
        }
        if (trimmed.length() > 60) {
            throw new BadRequestException("Customer name is too long (max 60 characters)");
        }
        return trimmed;
    }

    public static void requireCanHold(SRTOrder order) {
        requireUnsent(order, "Only an order that has not been sent to the kitchen can be held");
        if (OrderStatus.parse(order.getStatus()) != OrderStatus.NEW) {
            throw conflict("Only a new order can be held");
        }
    }

    public static void requireCanResume(SRTOrder order) {
        if (OrderStatus.parse(order.getStatus()) != OrderStatus.HELD) {
            throw conflict("Only a held order can be resumed");
        }
    }

    /** To-go orders may be sent before payment; dine-in orders only once paid. */
    public static void requireCanSend(SRTOrder order) {
        requireUnsent(order, "Order was already sent to the kitchen");
        if (OrderStatus.parse(order.getStatus()) != OrderStatus.NEW) {
            throw conflict("Only a new order can be sent to the kitchen (resume a held order first)");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw conflict("An order with no items cannot be sent to the kitchen");
        }
        OrderType type = OrderType.parse(order.getOrderType());
        if (type == OrderType.DINE_IN && !PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
            throw conflict("A dine-in order must be paid before it is sent to the kitchen");
        }
    }

    /** An order can be discarded only while it is unsent and nothing was paid. */
    public static void requireCanDiscard(SRTOrder order) {
        requireUnsent(order, "An order already sent to the kitchen cannot be discarded");
        OrderStatus status = OrderStatus.parse(order.getStatus());
        if (status != OrderStatus.NEW && status != OrderStatus.HELD) {
            throw conflict("Only a new or held order can be discarded");
        }
        if (!PaymentStatus.UNPAID.name().equals(order.getPaymentStatus())) {
            throw conflict("A paid order cannot be discarded, refund it instead");
        }
    }

    /** Pickup (status COMPLETED) is allowed only from READY. */
    public static void requireCanPickUp(SRTOrder order) {
        if (OrderStatus.parse(order.getStatus()) != OrderStatus.READY) {
            throw conflict("Only a ready order can be picked up");
        }
    }

    private static void requireUnsent(SRTOrder order, String message) {
        if (order.getSentToKitchenAt() != null) {
            throw conflict(message);
        }
    }

    private static ClientErrorException conflict(String message) {
        return new ClientErrorException(message, Response.Status.CONFLICT);
    }
}
