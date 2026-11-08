package com.spiceroad.service;

import com.spiceroad.config.OrderSettings;
import com.spiceroad.model.SRTOrder;
import com.spiceroad.repository.SRTDishRepository;
import com.spiceroad.repository.SRTOrderRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * What the counter and kiosk screens do: create an order, change its items, hold, resume, send it
 * to the kitchen, discard it and hand it over. The rules live in {@link CounterOrderPolicy}; every
 * write is conditional on the state that was read, so two screens cannot overwrite each other.
 */
@ApplicationScoped
public class CounterService {

    private static final Logger LOG = Logger.getLogger(CounterService.class);
    private static final Pattern ORDER_KEY = Pattern.compile("\\d{4}-\\d{2}-\\d{2}_[A-Z]{2,3}-\\d{3,}");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    @Inject
    SRTOrderRepository orderRepository;

    @Inject
    SRTDishRepository dishRepository;

    @Inject
    OrderSettings settings;

    private PricingCalculator pricing;
    private Clock clock;

    @PostConstruct
    void init() {
        pricing = new PricingCalculator(settings.taxRate(), settings.maxLines(), settings.maxQuantity());
        clock = Clock.system(ZoneId.of(settings.timezone()));
        BusinessDay.requireValidStartHour(settings.businessDayStartHour());
    }

    /** Creates an unsent, unpaid order. It does not reach the kitchen until {@link #send}. */
    public SRTOrder create(CounterOrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Order is required");
        }
        OrderSource source = CounterOrderPolicy.parseCounterSource(request.source());
        OrderType type = CounterOrderPolicy.parseOrderType(request.orderType());
        String table = CounterOrderPolicy.cleanTableNumber(type, request.tableNumber());
        String name = CounterOrderPolicy.cleanCustomerName(request.customerName());
        String phone = PhoneNumbers.normalize(request.customerPhone());
        PricingCalculator.PricedOrder priced = pricing.price(request.items(), dishRepository::getDishById);

        ZonedDateTime now = ZonedDateTime.now(clock);
        String businessDate = BusinessDay.of(now, settings.businessDayStartHour()).toString();
        String orderNumber = source.formatNumber(orderRepository.getNextOrderCount(businessDate));

        SRTOrder order = OrderAssembler.assemble(new OrderAssembler.Customer(name, null, null, phone),
                priced, source, type, businessDate, orderNumber, now.format(TIME_FORMAT), null);
        order.setTableNumber(table);
        try {
            SRTOrder saved = orderRepository.saveNewOrder(order);
            LOG.infof("Counter order created: %s %s items=%d", orderNumber, type, priced.items().size());
            return saved;
        } catch (ConditionalCheckFailedException e) {
            LOG.errorf("Order key collision for %s", order.getSk());
            throw new ClientErrorException("Could not create the order, please try again", Response.Status.CONFLICT);
        }
    }

    /** {@code open}: every order that is not final (including held and unsent). {@code past}: one day's finished orders. */
    public List<SRTOrder> list(String state, String date, Integer limit, String after) {
        int pageSize = OrderService.pageSize(limit);
        String requested = state == null || state.isBlank() ? "open" : state.trim().toLowerCase();
        if (requested.equals("open")) {
            return orderRepository.getOpenOrders(pageSize, after, false);
        }
        if (requested.equals("past")) {
            String businessDate = date == null || date.isBlank()
                    ? BusinessDay.of(ZonedDateTime.now(clock), settings.businessDayStartHour()).toString()
                    : OrderService.parseDate(date);
            return orderRepository.getOrdersForDate(businessDate, pageSize, after).stream()
                    .filter(o -> OrderStatus.parse(o.getStatus()).isFinal())
                    .toList();
        }
        throw new BadRequestException("state must be open or past");
    }

    public SRTOrder get(String sk) {
        return load(sk);
    }

    /** Replaces the items of an unsent, unpaid order. The server prices them again. */
    public SRTOrder replaceItems(String sk, Integer orderVersion, List<SRTOrder.OrderItem> items) {
        SRTOrder order = load(sk);
        CounterOrderPolicy.requireEditable(order);
        CounterOrderPolicy.requireVersion(order, orderVersion);
        PricingCalculator.PricedOrder priced = pricing.price(items, dishRepository::getDishById);
        List<SRTOrder.OrderItem> lines = OrderAssembler.toLines(priced);
        return guarded(() -> orderRepository.replaceItems(sk, order.getOrderVersion(), lines,
                priced.subtotal(), priced.tax(), priced.total()), order, "items changed");
    }

    public SRTOrder hold(String sk) {
        SRTOrder order = load(sk);
        CounterOrderPolicy.requireCanHold(order);
        return guarded(() -> orderRepository.transition(sk, OrderStatus.NEW.name(), order.getOrderVersion(),
                OrderStatus.HELD.name(), null, false), order, "held");
    }

    public SRTOrder resume(String sk) {
        SRTOrder order = load(sk);
        CounterOrderPolicy.requireCanResume(order);
        return guarded(() -> orderRepository.transition(sk, OrderStatus.HELD.name(), order.getOrderVersion(),
                OrderStatus.NEW.name(), null, false), order, "resumed");
    }

    /** The screen must send the version it saw, so an order edited elsewhere is not sent by mistake. */
    public SRTOrder send(String sk, Integer orderVersion) {
        SRTOrder order = load(sk);
        CounterOrderPolicy.requireVersion(order, orderVersion);
        CounterOrderPolicy.requireCanSend(order);
        String sentAt = ZonedDateTime.now(clock).toInstant().toString();
        return guarded(() -> orderRepository.transition(sk, OrderStatus.NEW.name(), order.getOrderVersion(),
                OrderStatus.NEW.name(), sentAt, false), order, "sent to kitchen");
    }

    /** Soft cancel: the record stays and the order number is never reused. */
    public SRTOrder discard(String sk) {
        SRTOrder order = load(sk);
        CounterOrderPolicy.requireCanDiscard(order);
        return guarded(() -> orderRepository.transition(sk, order.getStatus(), order.getOrderVersion(),
                OrderStatus.CANCELLED.name(), null, true), order, "discarded");
    }

    /** Hands a READY order to the customer; the order is then closed (COMPLETED). */
    public SRTOrder pickUp(String sk) {
        SRTOrder order = load(sk);
        CounterOrderPolicy.requireCanPickUp(order);
        return guarded(() -> orderRepository.updateStatus(sk, OrderStatus.READY.name(),
                OrderStatus.COMPLETED.name(), true), order, "picked up");
    }

    private SRTOrder load(String sk) {
        if (sk == null || !ORDER_KEY.matcher(sk).matches()) {
            throw new BadRequestException("Order key is not valid");
        }
        SRTOrder order = orderRepository.getOrder(sk);
        if (order == null) {
            throw new NotFoundException("Order not found");
        }
        return order;
    }

    private SRTOrder guarded(Supplier<SRTOrder> write, SRTOrder before, String action) {
        try {
            SRTOrder updated = write.get();
            LOG.infof("Order %s %s", before.getOrderNumber(), action);
            return updated;
        } catch (ConditionalCheckFailedException e) {
            throw new ClientErrorException("Order was changed by someone else, please refresh", Response.Status.CONFLICT);
        }
    }
}
