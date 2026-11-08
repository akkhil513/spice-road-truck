package com.spiceroad.repository;

import com.spiceroad.config.DynamoDbConfig;
import com.spiceroad.config.SrtTables;
import com.spiceroad.model.SRTCustomer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/** Returning customers, keyed by E.164 phone number (one GetItem, no index). */
@ApplicationScoped
public class SRTCustomerRepository {

    @Inject
    DynamoDbEnhancedClient enhancedClient;

    @Inject
    SrtTables tables;

    private DynamoDbTable<SRTCustomer> table() {
        return enhancedClient.table(tables.table(), DynamoDbConfig.CUSTOMER_TABLE_SCHEMA);
    }

    /** The saved customer for this E.164 phone number, or null if none. */
    public SRTCustomer findByPhone(String phoneE164) {
        return table().getItem(Key.builder().partitionValue(SRTCustomer.PK).sortValue(phoneE164).build());
    }

    public SRTCustomer save(SRTCustomer customer) {
        table().putItem(customer);
        return customer;
    }
}
