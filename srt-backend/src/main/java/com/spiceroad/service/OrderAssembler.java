package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import com.spiceroad.repository.SRTOrderRepository;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a brand-new order from already validated inputs. Everything that decides money,
 * progress or identity (keys, status, payment, item status, version) is set here by the server;
 * nothing is copied from the client's request.
 */
public final class OrderAssembler {

    public static final String ORDER_PK = "ORDER";
    public static final int CURRENT_ENTITY_VERSION = 2;

    private OrderAssembler() {
    }

    /** Customer details already cleaned and validated by the caller. */
    public record Customer(String name, String email, String cardLast4, String phone) {
    }

    public static SRTOrder assemble(Customer customer,
                                    PricingCalculator.PricedOrder priced,
                                    OrderSource source,
                                    OrderType orderType,
                                    String businessDate,
                                    String orderNumber,
                                    String orderTime,
                                    String sentToKitchenAt) {
        List<OrderItem> items = getOrderItems(priced);

        SRTOrder order = new SRTOrder();
        order.setPk(ORDER_PK);
        order.setSk(SRTOrderRepository.sortKey(businessDate, orderNumber));
        order.setOrderNumber(orderNumber);
        order.setCustomerName(customer.name());
        order.setCustomerEmail(customer.email());
        order.setCardLast4(customer.cardLast4());
        order.setCustomerPhone(customer.phone());
        order.setItems(items);
        order.setSubtotal(priced.subtotal().doubleValue());
        order.setTax(priced.tax().doubleValue());
        order.setGrandTotal(priced.total().doubleValue());
        order.setOrderDate(businessDate);
        order.setOrderTime(orderTime);
        order.setStatus(OrderStatus.NEW.name());
        order.setSource(source.name());
        order.setOrderType(orderType.name());
        order.setPaymentStatus(PaymentStatus.UNPAID.name());
        order.setPayments(new ArrayList<>());
        order.setOrderVersion(1);
        order.setSentToKitchenAt(sentToKitchenAt);
        order.setKitchenRevision(0);
        order.setGsiOpen(SRTOrder.OPEN);
        order.setEntityVersion(CURRENT_ENTITY_VERSION);
        return order;
    }

    @Nonnull
    private static List<OrderItem> getOrderItems(PricingCalculator.PricedOrder priced) {
        List<OrderItem> items = new ArrayList<>(priced.items().size());
        int line = 1;
        for (OrderItem pricedItem : priced.items()) {
            OrderItem item = new OrderItem();
            item.setId(pricedItem.getId());
            item.setName(pricedItem.getName());
            item.setPrice(pricedItem.getPrice());
            item.setQuantity(pricedItem.getQuantity());
            item.setLineId("L" + line++);
            item.setItemStatus(ItemStatus.PENDING.name());
            items.add(item);
        }
        return items;
    }
}
