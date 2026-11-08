package com.spiceroad.service;

import com.spiceroad.model.SRTOrder.OrderItem;

import java.util.List;

/**
 * What a counter or kiosk screen may send when creating an order. Only item ids and quantities
 * are read from the items; prices, totals, status and keys are always set by the server.
 */
public record CounterOrderRequest(String source, String orderType, String tableNumber,
                                  String customerName, String customerPhone, List<OrderItem> items) {
}
