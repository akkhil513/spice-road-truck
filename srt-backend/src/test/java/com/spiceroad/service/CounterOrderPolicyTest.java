package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CounterOrderPolicyTest {

    private static SRTOrder order(OrderStatus status, OrderType type, PaymentStatus payment, String sentAt) {
        SRTOrder o = new SRTOrder();
        o.setStatus(status.name());
        o.setOrderType(type.name());
        o.setPaymentStatus(payment.name());
        o.setSentToKitchenAt(sentAt);
        o.setItems(List.of(new SRTOrder.OrderItem()));
        return o;
    }

    private static SRTOrder fresh() {
        return order(OrderStatus.NEW, OrderType.TO_GO, PaymentStatus.UNPAID, null);
    }

    private static void assertConflict(Runnable action) {
        ClientErrorException e = assertThrows(ClientErrorException.class, action::run);
        assertEquals(409, e.getResponse().getStatus());
    }

    @Test
    void editableOnlyWhileUnsentAndNewOrHeld() {
        assertDoesNotThrow(() -> CounterOrderPolicy.requireEditable(fresh()));
        assertDoesNotThrow(() -> CounterOrderPolicy.requireEditable(
                order(OrderStatus.HELD, OrderType.TO_GO, PaymentStatus.UNPAID, null)));
        assertConflict(() -> CounterOrderPolicy.requireEditable(
                order(OrderStatus.NEW, OrderType.TO_GO, PaymentStatus.UNPAID, "2026-11-08T17:00:00Z")));
        assertConflict(() -> CounterOrderPolicy.requireEditable(
                order(OrderStatus.CANCELLED, OrderType.TO_GO, PaymentStatus.UNPAID, null)));
    }

    @Test
    void holdAndResume() {
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanHold(fresh()));
        assertConflict(() -> CounterOrderPolicy.requireCanHold(
                order(OrderStatus.HELD, OrderType.TO_GO, PaymentStatus.UNPAID, null)));
        assertConflict(() -> CounterOrderPolicy.requireCanHold(
                order(OrderStatus.NEW, OrderType.TO_GO, PaymentStatus.UNPAID, "2026-11-08T17:00:00Z")));
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanResume(
                order(OrderStatus.HELD, OrderType.TO_GO, PaymentStatus.UNPAID, null)));
        assertConflict(() -> CounterOrderPolicy.requireCanResume(fresh()));
    }

    @Test
    void toGoCanBeSentUnpaid() {
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanSend(fresh()));
    }

    @Test
    void dineInNeedsPaymentBeforeSend() {
        assertConflict(() -> CounterOrderPolicy.requireCanSend(
                order(OrderStatus.NEW, OrderType.DINE_IN, PaymentStatus.UNPAID, null)));
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanSend(
                order(OrderStatus.NEW, OrderType.DINE_IN, PaymentStatus.PAID, null)));
    }

    @Test
    void cannotSendTwiceHeldOrEmpty() {
        assertConflict(() -> CounterOrderPolicy.requireCanSend(
                order(OrderStatus.NEW, OrderType.TO_GO, PaymentStatus.UNPAID, "2026-11-08T17:00:00Z")));
        assertConflict(() -> CounterOrderPolicy.requireCanSend(
                order(OrderStatus.HELD, OrderType.TO_GO, PaymentStatus.UNPAID, null)));
        SRTOrder empty = fresh();
        empty.setItems(List.of());
        assertConflict(() -> CounterOrderPolicy.requireCanSend(empty));
    }

    @Test
    void discardOnlyUnsentUnpaid() {
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanDiscard(fresh()));
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanDiscard(
                order(OrderStatus.HELD, OrderType.TO_GO, PaymentStatus.UNPAID, null)));
        assertConflict(() -> CounterOrderPolicy.requireCanDiscard(
                order(OrderStatus.NEW, OrderType.TO_GO, PaymentStatus.PAID, null)));
        assertConflict(() -> CounterOrderPolicy.requireCanDiscard(
                order(OrderStatus.PREPARING, OrderType.TO_GO, PaymentStatus.UNPAID, "2026-11-08T17:00:00Z")));
    }

    @Test
    void pickUpOnlyFromReady() {
        assertDoesNotThrow(() -> CounterOrderPolicy.requireCanPickUp(
                order(OrderStatus.READY, OrderType.TO_GO, PaymentStatus.UNPAID, "2026-11-08T17:00:00Z")));
        assertConflict(() -> CounterOrderPolicy.requireCanPickUp(
                order(OrderStatus.PREPARING, OrderType.TO_GO, PaymentStatus.UNPAID, "2026-11-08T17:00:00Z")));
    }

    @Test
    void paidOrderItemsCannotChange() {
        assertConflict(() -> CounterOrderPolicy.requireEditable(
                order(OrderStatus.NEW, OrderType.DINE_IN, PaymentStatus.PAID, null)));
    }

    @Test
    void versionMustBeSentAndMatch() {
        SRTOrder o = fresh();
        o.setOrderVersion(3);
        assertDoesNotThrow(() -> CounterOrderPolicy.requireVersion(o, 3));
        assertConflict(() -> CounterOrderPolicy.requireVersion(o, 2));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.requireVersion(o, null));
    }

    @Test
    void onlyCounterAndKioskSourcesAreAllowed() {
        assertEquals(OrderSource.COUNTER, CounterOrderPolicy.parseCounterSource("counter"));
        assertEquals(OrderSource.KIOSK, CounterOrderPolicy.parseCounterSource("KIOSK"));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.parseCounterSource("DOORDASH"));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.parseCounterSource("nope"));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.parseCounterSource(null));
    }

    @Test
    void tableNumberRules() {
        assertEquals("7", CounterOrderPolicy.cleanTableNumber(OrderType.DINE_IN, " 7 "));
        assertNull(CounterOrderPolicy.cleanTableNumber(OrderType.TO_GO, null));
        assertNull(CounterOrderPolicy.cleanTableNumber(OrderType.TO_GO, "  "));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.cleanTableNumber(OrderType.DINE_IN, null));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.cleanTableNumber(OrderType.DINE_IN, "0"));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.cleanTableNumber(OrderType.DINE_IN, "100"));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.cleanTableNumber(OrderType.TO_GO, "5"));
    }

    @Test
    void customerNameDefaultsToWalkIn() {
        assertEquals("Walk-in", CounterOrderPolicy.cleanCustomerName(null));
        assertEquals("Walk-in", CounterOrderPolicy.cleanCustomerName("   "));
        assertEquals("Ravi", CounterOrderPolicy.cleanCustomerName(" Ravi "));
        assertThrows(BadRequestException.class, () -> CounterOrderPolicy.cleanCustomerName("x".repeat(61)));
    }
}
