package com.spiceroad.service;

import com.spiceroad.model.SRTDish;
import com.spiceroad.model.SRTOrder.OrderItem;
import jakarta.ws.rs.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Prices an order from the server's own menu. Client-supplied names, prices and
 * totals are never used; only item ids and quantities are read from the request.
 */
public final class PricingCalculator {

    private static final int MAX_ID_LENGTH = 64;
    private static final BigDecimal MAX_TAX_RATE = new BigDecimal("0.20");

    private final BigDecimal taxRate;
    private final int maxLines;
    private final int maxQuantity;

    public PricingCalculator(BigDecimal taxRate, int maxLines, int maxQuantity) {
        if (taxRate == null || taxRate.signum() < 0 || taxRate.compareTo(MAX_TAX_RATE) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 0.20");
        }
        if (maxLines < 1 || maxQuantity < 1) {
            throw new IllegalArgumentException("Order limits must be positive");
        }
        this.taxRate = taxRate;
        this.maxLines = maxLines;
        this.maxQuantity = maxQuantity;
    }

    public PricedOrder price(List<OrderItem> requested, Function<String, SRTDish> dishLookup) {
        if (requested == null || requested.isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem line : requested) {
            if (line == null || line.getId() == null || line.getId().isBlank()
                    || line.getId().length() > MAX_ID_LENGTH) {
                throw new BadRequestException("Every item needs a valid id");
            }
            if (line.getQuantity() < 1 || line.getQuantity() > maxQuantity) {
                throw new BadRequestException("Quantity must be between 1 and " + maxQuantity);
            }
            quantities.merge(line.getId(), line.getQuantity(), Integer::sum);
        }
        if (quantities.size() > maxLines) {
            throw new BadRequestException("An order can have at most " + maxLines + " different items");
        }

        List<OrderItem> priced = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            if (entry.getValue() > maxQuantity) {
                throw new BadRequestException("Quantity must be between 1 and " + maxQuantity);
            }
            SRTDish dish = dishLookup.apply(entry.getKey());
            if (dish == null) {
                throw new BadRequestException("Item is not on the menu: " + entry.getKey());
            }
            BigDecimal unitPrice = BigDecimal.valueOf(dish.getPrice()).setScale(2, RoundingMode.HALF_UP);
            if (unitPrice.signum() <= 0) {
                throw new BadRequestException("Item is not available: " + entry.getKey());
            }
            OrderItem item = new OrderItem();
            item.setId(dish.getId() != null ? dish.getId() : entry.getKey());
            item.setName(dish.getName());
            item.setPrice(unitPrice.doubleValue());
            item.setQuantity(entry.getValue());
            priced.add(item);
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(entry.getValue())));
        }

        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        return new PricedOrder(priced, subtotal, tax, subtotal.add(tax));
    }

    public record PricedOrder(List<OrderItem> items, BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }
}
