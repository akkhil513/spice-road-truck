package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KitchenOrderTest {

    @Test
    void showsOnlyWhatTheKitchenNeeds() {
        OrderItem item = new OrderItem();
        item.setId("dish-1");
        item.setLineId("L1");
        item.setName("Chicken Dum Biryani");
        item.setPrice(9.99);
        item.setQuantity(2);
        item.setItemStatus("TICKED");

        SRTOrder order = new SRTOrder();
        order.setSk("2026-11-08_SRT-001");
        order.setOrderNumber("SRT-001");
        order.setSource("ONLINE");
        order.setOrderType("TO_GO");
        order.setCustomerName("Akki");
        order.setCustomerEmail("secret@example.com");
        order.setCustomerPhone("+14045550142");
        order.setCardLast4("4242");
        order.setGrandTotal(21.58);
        order.setStatus("PREPARING");
        order.setOrderTime("11:42 AM");
        order.setSentToKitchenAt("2026-11-08T16:42:00Z");
        order.setItems(List.of(item));

        KitchenOrder view = KitchenOrder.from(order);

        assertEquals("SRT-001", view.orderNumber());
        assertEquals("Akki", view.customerName());
        assertEquals(1, view.items().size());
        assertEquals("L1", view.items().get(0).lineId());
        assertEquals(2, view.items().get(0).quantity());
        assertEquals("TICKED", view.items().get(0).itemStatus());

        String text = view.toString();
        assertFalse(text.contains("secret@example.com"));
        assertFalse(text.contains("+14045550142"));
        assertFalse(text.contains("4242"));
        assertFalse(text.contains("21.58"));
        assertFalse(text.contains("9.99"));
    }

    @Test
    void handlesMissingItemsAndStoredStatuses() {
        SRTOrder empty = new SRTOrder();
        assertEquals(List.of(), KitchenOrder.from(empty).items());

        OrderItem old = new OrderItem();
        old.setLineId("L1");
        SRTOrder order = new SRTOrder();
        order.setItems(Arrays.asList(old));
        assertEquals("PENDING", KitchenOrder.from(order).items().get(0).itemStatus());
    }
}
