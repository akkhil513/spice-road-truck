package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemTransitionTest {

    private static OrderItem line(String lineId, String itemStatus) {
        OrderItem item = new OrderItem();
        item.setLineId(lineId);
        item.setName("Dish " + lineId);
        item.setQuantity(1);
        item.setItemStatus(itemStatus);
        return item;
    }

    private static SRTOrder order(String status, String sentAt, Integer revision, OrderItem... lines) {
        SRTOrder order = new SRTOrder();
        order.setSk("2026-11-08_SRT-001");
        order.setStatus(status);
        order.setSentToKitchenAt(sentAt);
        order.setKitchenRevision(revision);
        order.setItems(new ArrayList<>(List.of(lines)));
        return order;
    }

    private static SRTOrder openOrder(OrderItem... lines) {
        return order("NEW", "2026-11-08T16:00:00Z", 0, lines);
    }

    @Test
    void firstTickStartsPreparing() {
        SRTOrder order = openOrder(line("L1", "PENDING"), line("L2", "PENDING"));
        ItemTransition.Plan plan = ItemTransition.plan(order, "L1", ItemStatus.PENDING, ItemStatus.TICKED);
        assertEquals(0, plan.index());
        assertEquals(OrderStatus.NEW, plan.seenOrderStatus());
        assertEquals(OrderStatus.PREPARING, plan.newOrderStatus());
        assertEquals(Integer.valueOf(0), plan.seenRevision());
    }

    @Test
    void clearingTheLastLineMakesTheOrderReady() {
        SRTOrder order = order("PREPARING", "2026-11-08T16:00:00Z", 3,
                line("L1", "CLEARED"), line("L2", "TICKED"));
        ItemTransition.Plan plan = ItemTransition.plan(order, "L2", ItemStatus.TICKED, ItemStatus.CLEARED);
        assertEquals(1, plan.index());
        assertEquals(OrderStatus.READY, plan.newOrderStatus());
    }

    @Test
    void recallingAReadyOrderLineReopensIt() {
        SRTOrder order = order("READY", "2026-11-08T16:00:00Z", 4,
                line("L1", "CLEARED"), line("L2", "CLEARED"));
        ItemTransition.Plan plan = ItemTransition.plan(order, "L2", ItemStatus.CLEARED, ItemStatus.TICKED);
        assertEquals(OrderStatus.PREPARING, plan.newOrderStatus());
    }

    @Test
    void rejectsWhenTheItemIsNotInTheStateTheClientSaw() {
        SRTOrder order = openOrder(line("L1", "TICKED"));
        ClientErrorException e = assertThrows(ClientErrorException.class,
                () -> ItemTransition.plan(order, "L1", ItemStatus.PENDING, ItemStatus.TICKED));
        assertEquals(409, e.getResponse().getStatus());
    }

    @Test
    void rejectsSkippingAStep() {
        SRTOrder order = openOrder(line("L1", "PENDING"));
        assertThrows(BadRequestException.class,
                () -> ItemTransition.plan(order, "L1", ItemStatus.PENDING, ItemStatus.CLEARED));
    }

    @Test
    void rejectsUnknownLine() {
        SRTOrder order = openOrder(line("L1", "PENDING"));
        assertThrows(NotFoundException.class,
                () -> ItemTransition.plan(order, "L9", ItemStatus.PENDING, ItemStatus.TICKED));
    }

    @Test
    void rejectsFinishedOrders() {
        for (String status : List.of("COMPLETED", "CANCELLED")) {
            SRTOrder order = order(status, "2026-11-08T16:00:00Z", 2, line("L1", "CLEARED"));
            ClientErrorException e = assertThrows(ClientErrorException.class,
                    () -> ItemTransition.plan(order, "L1", ItemStatus.CLEARED, ItemStatus.TICKED));
            assertEquals(409, e.getResponse().getStatus());
        }
    }

    @Test
    void rejectsOrdersNotSentToTheKitchen() {
        SRTOrder order = order("NEW", null, 0, line("L1", "PENDING"));
        ClientErrorException e = assertThrows(ClientErrorException.class,
                () -> ItemTransition.plan(order, "L1", ItemStatus.PENDING, ItemStatus.TICKED));
        assertEquals(409, e.getResponse().getStatus());
    }

    @Test
    void ordersWithoutARevisionYetPlanWithNull() {
        SRTOrder order = order("NEW", "2026-11-08T16:00:00Z", null, line("L1", "PENDING"));
        assertNull(ItemTransition.plan(order, "L1", ItemStatus.PENDING, ItemStatus.TICKED).seenRevision());
    }
}
