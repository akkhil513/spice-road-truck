package com.spiceroad.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SRTDish {
    String pk;
    String sk;
    String id;
    String name;
    String description;
    double price;
    String category;
    String imageUrl;

    @DynamoDbPartitionKey
    public String  getPk() {
        return pk;
    }
    @DynamoDbSortKey
    public String  getSk() {
        return sk;
    }
    @DynamoDbSecondaryPartitionKey(indexNames = "category-index")
    public String getCategory() { return category; }
}
