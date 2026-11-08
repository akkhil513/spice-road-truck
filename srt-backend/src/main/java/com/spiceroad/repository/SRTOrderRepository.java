package com.spiceroad.repository;

import com.spiceroad.config.DynamoDbConfig;
import com.spiceroad.config.SrtTables;
import com.spiceroad.model.SRTOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.List;
import java.util.Map;

/**
 * Orders live under one partition key (ORDER). The sort key is "{yyyy-MM-dd}_{orderNumber}",
 * so one business day is a single Query with begins_with, newest first. There are no Scans.
 */
@ApplicationScoped
public class SRTOrderRepository {

    private static final String ORDER_PK = "ORDER";
    private static final String COUNTER_PK = "COUNTER";
    private static final String COUNTER_SK_PREFIX = "order-counter#";
    private static final String OPEN_INDEX = "open-index";

    @Inject
    DynamoDbEnhancedClient enhancedClient;

    @Inject
    DynamoDbClient dynamoDbClient;

    @Inject
    SrtTables tables;

    private DynamoDbTable<SRTOrder> table() {
        return enhancedClient.table(tables.table(), DynamoDbConfig.ORDER_TABLE_SCHEMA);
    }

    public static String sortKey(String businessDate, String orderNumber) {
        return businessDate + "_" + orderNumber;
    }

    /**
     * Atomically increments the counter for the given business day and returns the new value.
     * One count is shared by every order source; the caller adds the source prefix.
     */
    public long getNextOrderCount(String businessDate) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tables.table())
                .key(Map.of(
                        "pk", AttributeValue.builder().s(COUNTER_PK).build(),
                        "sk", AttributeValue.builder().s(COUNTER_SK_PREFIX + businessDate).build()))
                .updateExpression("SET #count = if_not_exists(#count, :start) + :inc")
                .expressionAttributeNames(Map.of("#count", "count"))
                .expressionAttributeValues(Map.of(
                        ":start", AttributeValue.builder().n("0").build(),
                        ":inc", AttributeValue.builder().n("1").build()))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(request);
        return Long.parseLong(response.attributes().get("count").n());
    }

    /** Fails with ConditionalCheckFailedException if an order with this key already exists. */
    public SRTOrder saveNewOrder(SRTOrder order) {
        table().putItem(PutItemEnhancedRequest.builder(SRTOrder.class)
                .item(order)
                .conditionExpression(Expression.builder().expression("attribute_not_exists(sk)").build())
                .build());
        return order;
    }

    public SRTOrder getOrder(String sk) {
        return table().getItem(Key.builder().partitionValue(ORDER_PK).sortValue(sk).build());
    }

    /**
     * One page of a day's orders, newest first. Pass the last sort key of the previous page as
     * {@code after} to get the next page.
     */
    public List<SRTOrder> getOrdersForDate(String businessDate, int limit, String after) {
        QueryEnhancedRequest.Builder request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder().partitionValue(ORDER_PK).sortValue(businessDate + "_").build()))
                .scanIndexForward(false)
                .limit(limit);
        if (after != null && !after.isBlank()) {
            request.exclusiveStartKey(Map.of(
                    "pk", AttributeValue.builder().s(ORDER_PK).build(),
                    "sk", AttributeValue.builder().s(after).build()));
        }
        return table().query(request.build()).stream()
                .findFirst()
                .map(page -> page.items())
                .orElse(List.of());
    }

    /**
     * One page of open (not final) orders across all days, oldest first, read from the sparse
     * open-index. Pass the last sort key of the previous page as {@code after} for the next page.
     * With {@code sentToKitchenOnly} the orders not yet sent to the kitchen are filtered out; the
     * limit counts index entries read, so a page can hold fewer orders than the limit.
     */
    public List<SRTOrder> getOpenOrders(int limit, String after, boolean sentToKitchenOnly) {
        QueryRequest.Builder request = QueryRequest.builder()
                .tableName(tables.table())
                .indexName(OPEN_INDEX)
                .keyConditionExpression("#open = :open")
                .expressionAttributeNames(Map.of("#open", "gsiOpen"))
                .expressionAttributeValues(Map.of(":open", AttributeValue.builder().s(SRTOrder.OPEN).build()))
                .scanIndexForward(true)
                .limit(limit);
        if (sentToKitchenOnly) {
            request.filterExpression("attribute_exists(sentToKitchenAt)");
        }
        if (after != null && !after.isBlank()) {
            request.exclusiveStartKey(Map.of(
                    "pk", AttributeValue.builder().s(ORDER_PK).build(),
                    "sk", AttributeValue.builder().s(after).build(),
                    "gsiOpen", AttributeValue.builder().s(SRTOrder.OPEN).build()));
        }
        QueryResponse response = dynamoDbClient.query(request.build());
        return response.items().stream().map(DynamoDbConfig.ORDER_TABLE_SCHEMA::mapToItem).toList();
    }

    /**
     * Changes the status only if it is still {@code expectedStatus}, so two people editing at once
     * cannot silently overwrite each other. When the new status is final the order also leaves the
     * open-index. Throws ConditionalCheckFailedException on a mismatch.
     */
    public SRTOrder updateStatus(String sk, String expectedStatus, String newStatus, boolean closing) throws ConditionalCheckFailedException {
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tables.table())
                .key(Map.of(
                        "pk", AttributeValue.builder().s(ORDER_PK).build(),
                        "sk", AttributeValue.builder().s(sk).build()))
                .updateExpression(closing ? "SET #status = :new REMOVE gsiOpen" : "SET #status = :new")
                .conditionExpression("#status = :expected")
                .expressionAttributeNames(Map.of("#status", "status"))
                .expressionAttributeValues(Map.of(
                        ":new", AttributeValue.builder().s(newStatus).build(),
                        ":expected", AttributeValue.builder().s(expectedStatus).build()))
                .build());
        return getOrder(sk);
    }

    /**
     * Sets one line's kitchen status and the order status that follows from it, in one write.
     * The write only happens if that line is still in {@code from}, the order status and the
     * revision counter are unchanged since they were read; otherwise it throws
     * ConditionalCheckFailedException and nothing changes. {@code seenRevision} is null for
     * orders saved before the counter existed.
     */
    public SRTOrder updateItemStatus(String sk, int index, String lineId, String from, String to,
                                     String seenOrderStatus, String newOrderStatus, Integer seenRevision)
            throws ConditionalCheckFailedException {
        String line = "#items[" + index + "]";
        Map<String, String> names = Map.of(
                "#items", "items", "#is", "itemStatus", "#lid", "lineId",
                "#st", "status", "#rev", "kitchenRevision");
        Map<String, AttributeValue> values = new java.util.HashMap<>(Map.of(
                ":line", AttributeValue.builder().s(lineId).build(),
                ":from", AttributeValue.builder().s(from).build(),
                ":to", AttributeValue.builder().s(to).build(),
                ":seenStatus", AttributeValue.builder().s(seenOrderStatus).build(),
                ":newStatus", AttributeValue.builder().s(newOrderStatus).build(),
                ":nextRev", AttributeValue.builder().n(String.valueOf(seenRevision == null ? 1 : seenRevision + 1)).build()));
        String revisionCondition;
        if (seenRevision == null) {
            revisionCondition = "attribute_not_exists(#rev)";
        } else {
            revisionCondition = "#rev = :seenRev";
            values.put(":seenRev", AttributeValue.builder().n(String.valueOf(seenRevision)).build());
        }
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tables.table())
                .key(Map.of(
                        "pk", AttributeValue.builder().s(ORDER_PK).build(),
                        "sk", AttributeValue.builder().s(sk).build()))
                .updateExpression("SET " + line + ".#is = :to, #st = :newStatus, #rev = :nextRev")
                .conditionExpression(line + ".#lid = :line AND " + line + ".#is = :from AND #st = :seenStatus AND "
                        + revisionCondition)
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build());
        return getOrder(sk);
    }

    /**
     * Moves an unsent order to a new status (hold, resume, send, discard) and bumps its version, in
     * one write. It only happens if the order is still in {@code expectedStatus}, has the version
     * that was read and has not been sent to the kitchen; otherwise it throws
     * ConditionalCheckFailedException and nothing changes. {@code sentAt} is set when sending;
     * {@code closing} removes the order from the open-index.
     */
    public SRTOrder transition(String sk, String expectedStatus, Integer seenVersion, String newStatus,
                               String sentAt, boolean closing) throws ConditionalCheckFailedException {
        Map<String, AttributeValue> values = new java.util.HashMap<>();
        values.put(":expected", AttributeValue.builder().s(expectedStatus).build());
        values.put(":new", AttributeValue.builder().s(newStatus).build());
        values.put(":nextVer", AttributeValue.builder().n(String.valueOf(nextVersion(seenVersion))).build());
        StringBuilder update = new StringBuilder("SET #st = :new, #ver = :nextVer");
        if (sentAt != null) {
            update.append(", sentToKitchenAt = :sent");
            values.put(":sent", AttributeValue.builder().s(sentAt).build());
        }
        if (closing) {
            update.append(" REMOVE gsiOpen");
        }
        String condition = "#st = :expected AND attribute_not_exists(sentToKitchenAt) AND "
                + versionCondition(seenVersion, values);
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tables.table())
                .key(orderKey(sk))
                .updateExpression(update.toString())
                .conditionExpression(condition)
                .expressionAttributeNames(Map.of("#st", "status", "#ver", "orderVersion"))
                .expressionAttributeValues(values)
                .build());
        return getOrder(sk);
    }

    /**
     * Replaces the lines and totals of an unsent, unpaid NEW or HELD order and bumps its version.
     * Throws ConditionalCheckFailedException if the order changed since it was read.
     */
    public SRTOrder replaceItems(String sk, Integer seenVersion, List<SRTOrder.OrderItem> items,
                                 java.math.BigDecimal subtotal, java.math.BigDecimal tax,
                                 java.math.BigDecimal total) throws ConditionalCheckFailedException {
        Map<String, AttributeValue> values = new java.util.HashMap<>();
        values.put(":items", DynamoDbConfig.itemsToAttributeValue(items));
        values.put(":sub", AttributeValue.builder().n(subtotal.toPlainString()).build());
        values.put(":tax", AttributeValue.builder().n(tax.toPlainString()).build());
        values.put(":gt", AttributeValue.builder().n(total.toPlainString()).build());
        values.put(":nextVer", AttributeValue.builder().n(String.valueOf(nextVersion(seenVersion))).build());
        values.put(":new", AttributeValue.builder().s("NEW").build());
        values.put(":held", AttributeValue.builder().s("HELD").build());
        values.put(":unpaid", AttributeValue.builder().s("UNPAID").build());
        String condition = "(#st = :new OR #st = :held) AND attribute_not_exists(sentToKitchenAt) AND #pay = :unpaid AND "
                + versionCondition(seenVersion, values);
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tables.table())
                .key(orderKey(sk))
                .updateExpression("SET #items = :items, #sub = :sub, #tax = :tax, #gt = :gt, #ver = :nextVer")
                .conditionExpression(condition)
                .expressionAttributeNames(Map.of("#items", "items", "#sub", "subtotal", "#tax", "tax",
                        "#gt", "grandTotal", "#ver", "orderVersion", "#st", "status", "#pay", "paymentStatus"))
                .expressionAttributeValues(values)
                .build());
        return getOrder(sk);
    }

    private static Map<String, AttributeValue> orderKey(String sk) {
        return Map.of("pk", AttributeValue.builder().s(ORDER_PK).build(),
                "sk", AttributeValue.builder().s(sk).build());
    }

    private static int nextVersion(Integer seenVersion) {
        return seenVersion == null ? 1 : seenVersion + 1;
    }

    /** Orders saved before versions existed have no attribute, so a null version means "absent". */
    private static String versionCondition(Integer seenVersion, Map<String, AttributeValue> values) {
        if (seenVersion == null) {
            return "attribute_not_exists(#ver)";
        }
        values.put(":seenVer", AttributeValue.builder().n(String.valueOf(seenVersion)).build());
        return "#ver = :seenVer";
    }
}
