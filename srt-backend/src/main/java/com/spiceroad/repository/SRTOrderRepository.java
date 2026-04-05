package com.spiceroad.repository;

import com.spiceroad.model.SRTOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SRTOrderRepository {

    private static final String TABLE_NAME = "srt-orders-dev";
    private static final String ORDER_PK = "ORDER";
    private static final String COUNTER_PK = "COUNTER";
    private static final String COUNTER_SK = "order-counter";

    @Inject
    DynamoDbEnhancedClient enhancedClient;

    @Inject
    DynamoDbClient dynamoDbClient;

    private DynamoDbTable<SRTOrder> table() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(SRTOrder.class));
    }

    public String getNextOrderNumber() {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(java.util.Map.of(
                        "pk", AttributeValue.builder().s(COUNTER_PK).build(),
                        "sk", AttributeValue.builder().s(COUNTER_SK).build()
                ))
                .updateExpression("SET #count = if_not_exists(#count, :start) + :inc")
                .expressionAttributeNames(java.util.Map.of("#count", "count"))
                .expressionAttributeValues(java.util.Map.of(
                        ":start", AttributeValue.builder().n("0").build(),
                        ":inc", AttributeValue.builder().n("1").build()
                ))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(request);
        long count = Long.parseLong(response.attributes().get("count").n());
        return String.format("SRT-%04d", count);
    }

    public SRTOrder saveOrder(SRTOrder order) {
        table().putItem(order);
        return order;
    }

    public List<SRTOrder> getAllOrders() {
        return table().scan(ScanEnhancedRequest.builder().build())
                .items()
                .stream()
                .filter(o -> ORDER_PK.equals(o.getPk()))
                .collect(Collectors.toList());
    }

    public SRTOrder updateStatus(String sk, String status) {
        SRTOrder order = table().getItem(Key.builder()
                .partitionValue(ORDER_PK)
                .sortValue(sk)
                .build());
        if (order != null) {
            order.setStatus(status);
            table().putItem(order);
        }
        return order;
    }
}