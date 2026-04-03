package com.spiceroad.repository;

import com.spiceroad.model.SRTDish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SRTDishRepository {

    @Inject
    DynamoDbTable<SRTDish> dishTable;

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
        String id = "dish-" + java.util.UUID.randomUUID();
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

}