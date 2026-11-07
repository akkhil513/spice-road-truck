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
    private static final int CURRENT_ENTITY_VERSION = 1;
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
    }

    /**
     * Builds the stored order from scratch. Only item ids/quantities, the customer's name and
     * email, and the card's last four digits are taken from the request; prices, totals, status,
     * keys and timestamps all come from the server.
     */
    public SRTOrder placeOrder(SRTOrder request) {
        if (request == null) {
            throw new BadRequestException("Order is required");
        }
        String name = cleanName(request.getCustomerName());
        String email = cleanEmail(request.getCustomerEmail());
        String cardLast4 = cleanCardLast4(request.getCardLast4());

        PricingCalculator.PricedOrder priced = pricing.price(request.getItems(), dishRepository::getDishById);

        ZonedDateTime now = ZonedDateTime.now(clock);
        String businessDate = now.toLocalDate().toString();
        String orderNumber = orderRepository.getNextOrderNumber(businessDate);

        SRTOrder order = new SRTOrder();
        order.setPk("ORDER");
        order.setSk(SRTOrderRepository.sortKey(businessDate, orderNumber));
        order.setOrderNumber(orderNumber);
        order.setCustomerName(name);
        order.setCustomerEmail(email);
        order.setCardLast4(cardLast4);
        order.setItems(priced.items());
        order.setSubtotal(priced.subtotal().doubleValue());
        order.setTax(priced.tax().doubleValue());
        order.setGrandTotal(priced.total().doubleValue());
        order.setOrderDate(businessDate);
        order.setOrderTime(now.format(TIME_FORMAT));
        order.setStatus(OrderStatus.NEW.name());
        order.setEntityVersion(CURRENT_ENTITY_VERSION);

        try {
            SRTOrder saved = orderRepository.saveNewOrder(order);
            LOG.infof("Order placed: %s total=%s items=%d", orderNumber, priced.total(), priced.items().size());
            return saved;
        } catch (ConditionalCheckFailedException e) {
            LOG.errorf("Order key collision for %s", order.getSk());
            throw new ClientErrorException("Could not place the order, please try again", Response.Status.CONFLICT);
        }
    }

    /** One page of a business day's orders (today in the configured timezone if no date is given). */
    public List<SRTOrder> getOrders(String date, Integer limit, String after) {
        String businessDate = date == null || date.isBlank() ? LocalDate.now(clock).toString() : parseDate(date);
        int pageSize = limit == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(limit, MAX_PAGE_SIZE));
        return orderRepository.getOrdersForDate(businessDate, pageSize, after);
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
            SRTOrder updated = orderRepository.updateStatus(sk, from.name(), next.name());
            LOG.infof("Order %s: %s -> %s", current.getOrderNumber(), from, next);
            return updated;
        } catch (ConditionalCheckFailedException e) {
            throw new ClientErrorException("Order was changed by someone else, please refresh", Response.Status.CONFLICT);
        }
    }

    private static String parseDate(String date) {
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
