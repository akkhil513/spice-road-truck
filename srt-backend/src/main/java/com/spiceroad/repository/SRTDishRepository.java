package com.spiceroad.repository;

import com.spiceroad.model.SRTDish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.spiceroad.config.DynamoDbConfig.TABLE_NAME;

@ApplicationScoped
public class SRTDishRepository {

    @Inject
    DynamoDbTable<SRTDish> dishTable;

    @Inject
    DynamoDbClient dynamoDbClient;

    public List<SRTDish> getAllDishes() {
        return dishTable.query(QueryConditional.keyEqualTo(Key.builder().partitionValue("DISH").build())).items().stream().collect(Collectors.toList());
    }

    public List<SRTDish> getDishesByCategory(String category) {
        DynamoDbIndex<SRTDish> index = dishTable.index("category-index");
        return index.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(category).build())).stream().flatMap(page -> page.items().stream()).collect(Collectors.toList());
    }

    // Get one dish by id
    public SRTDish getDishById(String id) {
        Key key = Key.builder().partitionValue("DISH").sortValue(id).build();
        return dishTable.getItem(key);
    }

    // Create a new dish
    public SRTDish createDish(SRTDish dish) {
        String id = getNextDishId();  // dish-1, dish-2, dish-3...
        dish.setPk("DISH");
        dish.setSk(id);
        dish.setId(id);
        dishTable.putItem(dish);
        return dish;
    }

    // Update existing dish
    public SRTDish updateDish(String id, SRTDish dish) {
        dish.setPk("DISH");
        dish.setSk(id);
        dish.setId(id);
        return dishTable.updateItem(dish);
    }

    // Delete a dish
    public void deleteDish(String id) {
        Key key = Key.builder().partitionValue("DISH").sortValue(id).build();
        dishTable.deleteItem(key);
    }

    public String getNextDishId() {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of(
                        "pk", AttributeValue.builder().s("COUNTER").build(),
                        "sk", AttributeValue.builder().s("dish-counter").build()
                ))
                .updateExpression("ADD #count :increment")
                .expressionAttributeNames(Map.of("#count", "count"))
                .expressionAttributeValues(Map.of(
                        ":increment", AttributeValue.builder().n("1").build()
                ))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(request);
        String count = response.attributes().get("count").n();
        return "dish-" + count;
    }

}