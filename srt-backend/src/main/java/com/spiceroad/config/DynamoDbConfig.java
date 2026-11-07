package com.spiceroad.config;

import com.spiceroad.model.SRTDish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbConfig {
    private static final TableSchema<SRTDish> DISH_TABLE_SCHEMA = StaticTableSchema.builder(SRTDish.class)
            .newItemSupplier(SRTDish::new)
            .addAttribute(String.class, a -> a.name("pk")
                    .getter(SRTDish::getPk)
                    .setter(SRTDish::setPk)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
            .addAttribute(String.class, a -> a.name("sk")
                    .getter(SRTDish::getSk)
                    .setter(SRTDish::setSk)
                    .tags(StaticAttributeTags.primarySortKey()))
            .addAttribute(String.class, a -> a.name("category")
                    .getter(SRTDish::getCategory)
                    .setter(SRTDish::setCategory)
                    .tags(StaticAttributeTags.secondaryPartitionKey("category-index")))
            .addAttribute(String.class, a -> a.name("id")
                    .getter(SRTDish::getId)
                    .setter(SRTDish::setId))
            .addAttribute(String.class, a -> a.name("name")
                    .getter(SRTDish::getName)
                    .setter(SRTDish::setName))
            .addAttribute(String.class, a -> a.name("description")
                    .getter(SRTDish::getDescription)
                    .setter(SRTDish::setDescription))
            .addAttribute(Double.class, a -> a.name("price")
                    .getter(SRTDish::getPrice)
                    .setter(SRTDish::setPrice))
            .addAttribute(String.class, a -> a.name("imageUrl")
                    .getter(SRTDish::getImageUrl)
                    .setter(SRTDish::setImageUrl))
            .build();

    @Inject
    DynamoDbClient dynamoDbClient;

    @Produces
    @ApplicationScoped
    public DynamoDbEnhancedClient enhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Produces
    @ApplicationScoped
    public DynamoDbTable<SRTDish> dishTable(DynamoDbEnhancedClient enhancedClient, SrtTables tables) {
        return enhancedClient.table(tables.dishes(), DISH_TABLE_SCHEMA);
    }
}
