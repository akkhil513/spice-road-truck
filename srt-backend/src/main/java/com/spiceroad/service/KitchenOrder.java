package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;

import java.util.List;

/**
 * What the kitchen screen may see. Contact details, card reference, prices and payment data are
 * left out on purpose.
 */
public record KitchenOrder(String sk, String orderNumber, String source, String orderType,
                           String tableNumber, String customerName, String status, String orderTime,
                           String sentToKitchenAt, List<Item> items) {

    public record Item(String lineId, String name, int quantity, String itemStatus) {
    }

    public static KitchenOrder from(SRTOrder order) {
        List<Item> items = order.getItems() == null ? List.of() : order.getItems().stream()
                .map(i -> new Item(i.getLineId(), i.getName(), i.getQuantity(),
                        ItemStatus.ofStored(i.getItemStatus()).name()))
                .toList();
        return new KitchenOrder(order.getSk(), order.getOrderNumber(), order.getSource(),
                order.getOrderType(), order.getTableNumber(), order.getCustomerName(),
                order.getStatus(), order.getOrderTime(), order.getSentToKitchenAt(), items);
    }
}
