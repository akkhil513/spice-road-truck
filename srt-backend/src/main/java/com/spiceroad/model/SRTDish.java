package com.spiceroad.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@DynamoDbBean
public class SRTDish {
    private String pk;
    private String sk;
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;

    @DynamoDbPartitionKey
    @JsonIgnore
    public String  getPk() {
        return pk;
    }
    @DynamoDbSortKey
    @JsonIgnore
    public String  getSk() {
        return sk;
    }
    @DynamoDbSecondaryPartitionKey(indexNames = "category-index")
    public String getCategory() { return category; }

    @DynamoDbAttribute("id")
    public String getId() { return id; }

    @DynamoDbAttribute("name")
    public String getName() { return name; }

    @DynamoDbAttribute("description")
    public String getDescription() { return description; }

    @DynamoDbAttribute("price")
    public double getPrice() { return price; }

    @DynamoDbAttribute("imageUrl")
    public String getImageUrl() { return imageUrl; }

    public void setPk(String pk) { this.pk = pk; }

    public void setSk(String sk) { this.sk = sk; }

    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setPrice(double price) { this.price = price; }

    public void setCategory(String category) { this.category = category; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
