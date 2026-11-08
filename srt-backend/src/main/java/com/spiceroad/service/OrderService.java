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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);
    private static final int MAX_NAME_LENGTH = 60;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 200;
    private static final Pattern CARD_LAST4 = Pattern.compile("\\d{4}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
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

    /**
     * Builds the stored order from scratch. Only item ids/quantities, the customer's name, email
     * and phone, and the card's last four digits are taken from the request. Prices, totals, status,
     * source, order type, payment status, item status, keys and timestamps all come from the server;
     * this public route always creates an unpaid online to-go order, sent to the kitchen at once.
     */
    public SRTOrder placeOrder(SRTOrder request) {
        if (request == null) {
            throw new BadRequestException("Order is required");
        }
        String name = cleanName(request.getCustomerName());
        String email = cleanEmail(request.getCustomerEmail());
        String cardLast4 = cleanCardLast4(request.getCardLast4());
        String phone = PhoneNumbers.normalize(request.getCustomerPhone());

        PricingCalculator.PricedOrder priced = pricing.price(request.getItems(), dishRepository::getDishById);

        ZonedDateTime now = ZonedDateTime.now(clock);
        String businessDate = BusinessDay.of(now, settings.businessDayStartHour()).toString();
        OrderSource source = OrderSource.ONLINE;
        String orderNumber = source.formatNumber(orderRepository.getNextOrderCount(businessDate));

        SRTOrder order = OrderAssembler.assemble(
                new OrderAssembler.Customer(name, email, cardLast4, phone),
                priced, source, OrderType.TO_GO, businessDate, orderNumber, now.format(TIME_FORMAT),
                now.toInstant().toString());

        try {
            SRTOrder saved = orderRepository.saveNewOrder(order);
            LOG.infof("Order placed: %s total=%s items=%d", orderNumber, priced.total(), priced.items().size());
            return saved;
        } catch (ConditionalCheckFailedException e) {
            LOG.errorf("Order key collision for %s", order.getSk());
            throw new ClientErrorException("Could not place the order, please try again", Response.Status.CONFLICT);
        }
    }

    /** One page of a business day's orders (the current business day if no date is given). */
    public List<SRTOrder> getOrders(String date, Integer limit, String after) {
        String businessDate = date == null || date.isBlank()
                ? BusinessDay.of(ZonedDateTime.now(clock), settings.businessDayStartHour()).toString()
                : parseDate(date);
        return orderRepository.getOrdersForDate(businessDate, pageSize(limit), after);
    }

    static int pageSize(Integer limit) {
        return limit == null ? DEFAULT_PAGE_SIZE : Math.clamp(limit, 1, MAX_PAGE_SIZE);
    }

    public SRTOrder updateStatus(String sk, String requestedStatus) {
        OrderStatus next;
        try {
            next = OrderStatus.parse(requestedStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
        SRTOrder current = orderRepository.getOrder(sk);
        if (current == null) {
            throw new NotFoundException("Order not found");
        }
        OrderStatus from = OrderStatus.parse(current.getStatus());
        if (!from.canMoveTo(next)) {
            throw new ClientErrorException("Order is " + from + " and cannot move to " + next, Response.Status.CONFLICT);
        }
        try {
            SRTOrder updated = orderRepository.updateStatus(sk, from.name(), next.name(), next.isFinal());
            LOG.infof("Order %s: %s -> %s", current.getOrderNumber(), from, next);
            return updated;
        } catch (ConditionalCheckFailedException e) {
            throw new ClientErrorException("Order was changed by someone else, please refresh", Response.Status.CONFLICT);
        }
    }

    static String parseDate(String date) {
        try {
            return LocalDate.parse(date).toString();
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Date must look like 2026-11-06");
        }
    }

    private static String cleanName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("Customer name is required (max " + MAX_NAME_LENGTH + " characters)");
        }
        return trimmed;
    }

    private static String cleanEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (trimmed.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(trimmed).matches()) {
            throw new BadRequestException("Email address is not valid");
        }
        return trimmed;
    }

    private static String cleanCardLast4(String cardLast4) {
        if (cardLast4 == null || cardLast4.isBlank()) {
            return null;
        }
        if (!CARD_LAST4.matcher(cardLast4).matches()) {
            throw new BadRequestException("Card reference must be 4 digits");
        }
        return cardLast4;
    }
}
