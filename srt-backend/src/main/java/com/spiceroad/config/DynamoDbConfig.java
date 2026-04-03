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

    public static final String TABLE_NAME = System.getenv("TABLE_NAME") != null ?
            System.getenv("TABLE_NAME") :
            "srt-dishes-dev";

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
    public DynamoDbTable<SRTDish> dishTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(SRTDish.class));
    }
}
