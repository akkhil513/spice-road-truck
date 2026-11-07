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
    private static final String ORDER_NUMBER_PREFIX = "SRT-";

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

    /** Atomically increments the counter for the given business day; numbering restarts at 001 each day. */
    public String getNextOrderNumber(String businessDate) {
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
        long count = Long.parseLong(response.attributes().get("count").n());
        return String.format(ORDER_NUMBER_PREFIX + "%03d", count);
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
     * Changes the status only if it is still {@code expectedStatus}, so two people editing at once
     * cannot silently overwrite each other. Throws ConditionalCheckFailedException on a mismatch.
     */
    public SRTOrder updateStatus(String sk, String expectedStatus, String newStatus) throws ConditionalCheckFailedException {
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tables.table())
                .key(Map.of(
                        "pk", AttributeValue.builder().s(ORDER_PK).build(),
                        "sk", AttributeValue.builder().s(sk).build()))
                .updateExpression("SET #status = :new")
                .conditionExpression("#status = :expected")
                .expressionAttributeNames(Map.of("#status", "status"))
                .expressionAttributeValues(Map.of(
                        ":new", AttributeValue.builder().s(newStatus).build(),
                        ":expected", AttributeValue.builder().s(expectedStatus).build()))
                .build());
        return getOrder(sk);
    }
}
