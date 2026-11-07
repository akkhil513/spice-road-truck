package com.spiceroad.service;

import com.spiceroad.model.SRTDish;
import com.spiceroad.model.SRTOrder.OrderItem;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator(new BigDecimal("0.08"), 5, 10);

    private static SRTDish dish(String id, String name, double price) {
        SRTDish d = new SRTDish();
        d.setId(id);
        d.setName(name);
        d.setPrice(price);
        return d;
    }

    private final Map<String, SRTDish> menu = Map.of(
            "dish-1", dish("dish-1", "Chicken Tikka", 7.99),
            "dish-2", dish("dish-2", "Falooda", 6.99),
            "dish-3", dish("dish-3", "Free Item", 0.0));

    private final Function<String, SRTDish> lookup = menu::get;

    private static OrderItem line(String id, int qty) {
        OrderItem i = new OrderItem();
        i.setId(id);
        i.setQuantity(qty);
        i.setName("client says this");
        i.setPrice(0.01);
        return i;
    }

    @Test
    void usesServerPricesAndIgnoresClientPrices() {
        var result = calculator.price(List.of(line("dish-1", 2), line("dish-2", 1)), lookup);

        assertEquals(new BigDecimal("22.97"), result.subtotal());
        assertEquals(new BigDecimal("1.84"), result.tax());
        assertEquals(new BigDecimal("24.81"), result.total());
        assertEquals("Chicken Tikka", result.items().get(0).getName());
        assertEquals(7.99, result.items().get(0).getPrice());
    }

    @Test
    void mergesDuplicateLines() {
        var result = calculator.price(List.of(line("dish-1", 1), line("dish-1", 2)), lookup);

        assertEquals(1, result.items().size());
        assertEquals(3, result.items().get(0).getQuantity());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(), lookup));
        assertThrows(BadRequestException.class, () -> calculator.price(null, lookup));
    }

    @Test
    void rejectsUnknownItem() {
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line("nope", 1)), lookup));
    }

    @Test
    void rejectsZeroPricedItem() {
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line("dish-3", 1)), lookup));
    }

    @Test
    void rejectsBadQuantities() {
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line("dish-1", 0)), lookup));
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line("dish-1", -2)), lookup));
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line("dish-1", 11)), lookup));
        assertThrows(BadRequestException.class,
                () -> calculator.price(List.of(line("dish-1", 6), line("dish-1", 6)), lookup));
    }

    @Test
    void rejectsTooManyDifferentItems() {
        var many = List.of(line("a", 1), line("b", 1), line("c", 1), line("d", 1), line("e", 1), line("f", 1));
        assertThrows(BadRequestException.class, () -> calculator.price(many, lookup));
    }

    @Test
    void rejectsBlankOrOversizedId() {
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line(" ", 1)), lookup));
        assertThrows(BadRequestException.class, () -> calculator.price(List.of(line("x".repeat(65), 1)), lookup));
    }

    @Test
    void rejectsInvalidTaxRate() {
        assertThrows(IllegalArgumentException.class, () -> new PricingCalculator(new BigDecimal("-0.01"), 5, 10));
        assertThrows(IllegalArgumentException.class, () -> new PricingCalculator(new BigDecimal("0.5"), 5, 10));
    }
}
