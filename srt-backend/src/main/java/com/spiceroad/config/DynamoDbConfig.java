package com.spiceroad.config;

import com.spiceroad.model.SRTDish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbConfig {

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
        return enhancedClient.table(tables.dishes(), TableSchema.fromBean(SRTDish.class));
    }
}
