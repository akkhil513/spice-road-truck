package com.spiceroad.service;

import com.spiceroad.config.OrderSettings;
import com.spiceroad.model.SRTOrder;
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
import java.util.List;
import java.util.regex.Pattern;

/** What the kitchen screen reads and does: see open tickets, tick, clear and recall items. */
@ApplicationScoped
public class KitchenService {

    private static final Logger LOG = Logger.getLogger(KitchenService.class);
    private static final Pattern ORDER_KEY = Pattern.compile("\\d{4}-\\d{2}-\\d{2}_[A-Z]{2,3}-\\d{3,}");
    private static final Pattern LINE_ID = Pattern.compile("L\\d{1,3}");

    @Inject
    SRTOrderRepository orderRepository;

    @Inject
    OrderSettings settings;

    private Clock clock;

    @PostConstruct
    void init() {
        clock = Clock.system(ZoneId.of(settings.timezone()));
    }

    /**
     * Active tickets: open orders already sent to the kitchen, oldest first. With
     * {@code completed} the finished (completed or cancelled) kitchen orders of one business
     * day instead, newest first.
     */
    public List<KitchenOrder> getKitchenOrders(boolean completed, String date, Integer limit, String after) {
        int pageSize = OrderService.pageSize(limit);
        List<SRTOrder> orders;
        if (completed) {
            String businessDate = date == null || date.isBlank()
                    ? BusinessDay.of(ZonedDateTime.now(clock), settings.businessDayStartHour()).toString()
                    : OrderService.parseDate(date);
            orders = orderRepository.getOrdersForDate(businessDate, pageSize, after).stream()
                    .filter(o -> o.getSentToKitchenAt() != null && OrderStatus.parse(o.getStatus()).isFinal())
                    .toList();
        } else {
            orders = orderRepository.getOpenOrders(pageSize, after, true);
        }
        return orders.stream().map(KitchenOrder::from).toList();
    }

    public KitchenOrder setItemStatus(String sk, String lineId, String fromValue, String toValue) {
        if (sk == null || !ORDER_KEY.matcher(sk).matches()) {
            throw new BadRequestException("Order key is not valid");
        }
        if (lineId == null || !LINE_ID.matcher(lineId).matches()) {
            throw new BadRequestException("Item id is not valid");
        }
        ItemStatus from;
        ItemStatus to;
        try {
            from = ItemStatus.parse(fromValue);
            to = ItemStatus.parse(toValue);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        SRTOrder current = orderRepository.getOrder(sk);
        if (current == null) {
            throw new NotFoundException("Order not found");
        }
        ItemTransition.Plan plan = ItemTransition.plan(current, lineId, from, to);
        try {
            SRTOrder updated = orderRepository.updateItemStatus(sk, plan.index(), lineId, from.name(), to.name(),
                    plan.seenOrderStatus().name(), plan.newOrderStatus().name(), plan.seenRevision());
            LOG.infof("Order %s item %s: %s -> %s (order %s)", current.getOrderNumber(), lineId, from, to,
                    plan.newOrderStatus());
            return KitchenOrder.from(updated);
        } catch (ConditionalCheckFailedException e) {
            throw new ClientErrorException("Item was changed on another screen, please refresh", Response.Status.CONFLICT);
        }
    }
}
