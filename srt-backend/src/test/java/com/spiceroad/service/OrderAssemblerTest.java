package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderAssemblerTest {

    private static OrderItem priced(String id, String name, double price, int quantity) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setName(name);
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }

    private static SRTOrder assemble(OrderSource source, OrderType type) {
        PricingCalculator.PricedOrder pricedOrder = new PricingCalculator.PricedOrder(
                List.of(priced("dish-1", "Chicken Tikka", 7.99, 2), priced("dish-2", "Falooda", 6.99, 1)),
                new BigDecimal("22.97"), new BigDecimal("1.84"), new BigDecimal("24.81"));
        return OrderAssembler.assemble(
                new OrderAssembler.Customer("Akki", "a@example.com", "4242", "+14045550142"),
                pricedOrder, source, type, "2026-11-06", source.formatNumber(3), "08:41 PM");
    }

    @Test
    void setsKeysAndNumbering() {
        SRTOrder order = assemble(OrderSource.UBEREATS, OrderType.TO_GO);
        assertEquals("ORDER", order.getPk());
        assertEquals("2026-11-06_UE-003", order.getSk());
        assertEquals("UE-003", order.getOrderNumber());
        assertEquals("2026-11-06", order.getOrderDate());
        assertEquals("08:41 PM", order.getOrderTime());
    }

    @Test
    void startsAsNewUnpaidOpenOrder() {
        SRTOrder order = assemble(OrderSource.ONLINE, OrderType.TO_GO);
        assertEquals("NEW", order.getStatus());
        assertEquals("UNPAID", order.getPaymentStatus());
        assertTrue(order.getPayments().isEmpty());
        assertEquals(Integer.valueOf(1), order.getOrderVersion());
        assertEquals(OrderAssembler.CURRENT_ENTITY_VERSION, order.getEntityVersion());
        assertEquals(SRTOrder.OPEN, order.getGsiOpen());
        assertNull(order.getSentToKitchenAt());
        assertNull(order.getExternalOrderId());
        assertNull(order.getTableNumber());
    }

    @Test
    void recordsSourceAndTypeAsGiven() {
        SRTOrder order = assemble(OrderSource.COUNTER, OrderType.DINE_IN);
        assertEquals("COUNTER", order.getSource());
        assertEquals("DINE_IN", order.getOrderType());
    }

    @Test
    void numbersEveryLineAndStartsItemsPending() {
        SRTOrder order = assemble(OrderSource.ONLINE, OrderType.TO_GO);
        assertEquals(2, order.getItems().size());
        assertEquals("L1", order.getItems().get(0).getLineId());
        assertEquals("L2", order.getItems().get(1).getLineId());
        order.getItems().forEach(item -> assertEquals("PENDING", item.getItemStatus()));
        assertEquals("Chicken Tikka", order.getItems().get(0).getName());
        assertEquals(2, order.getItems().get(0).getQuantity());
    }

    @Test
    void carriesCustomerDetailsAndTotals() {
        SRTOrder order = assemble(OrderSource.ONLINE, OrderType.TO_GO);
        assertEquals("Akki", order.getCustomerName());
        assertEquals("+14045550142", order.getCustomerPhone());
        assertEquals(22.97, order.getSubtotal());
        assertEquals(1.84, order.getTax());
        assertEquals(24.81, order.getGrandTotal());
    }
}
