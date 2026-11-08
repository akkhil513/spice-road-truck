package com.spiceroad.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/** A returning customer, looked up by phone: pk = CUSTOMER, sk = E.164 phone number. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SRTCustomer {

    public static final String PK = "CUSTOMER";

    private String pk;
    private String sk;
    private String name;
    private boolean smsOptIn;
    private String createdAt;
    private int entityVersion;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbAttribute("name")
    public String getName() { return name; }

    @DynamoDbAttribute("smsOptIn")
    public boolean isSmsOptIn() { return smsOptIn; }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() { return createdAt; }

    @DynamoDbAttribute("entityVersion")
    public int getEntityVersion() { return entityVersion; }
}
