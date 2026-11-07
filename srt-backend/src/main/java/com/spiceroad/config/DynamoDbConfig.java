package com.spiceroad.config;

import com.spiceroad.model.SRTDish;
import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbConfig {
    private static final TableSchema<OrderItem> ORDER_ITEM_SCHEMA = StaticTableSchema.builder(OrderItem.class)
            .newItemSupplier(OrderItem::new)
            .addAttribute(String.class, a -> a.name("id")
                    .getter(OrderItem::getId)
                    .setter(OrderItem::setId))
            .addAttribute(String.class, a -> a.name("name")
                    .getter(OrderItem::getName)
                    .setter(OrderItem::setName))
            .addAttribute(Double.class, a -> a.name("price")
                    .getter(OrderItem::getPrice)
                    .setter(OrderItem::setPrice))
            .addAttribute(Integer.class, a -> a.name("quantity")
                    .getter(OrderItem::getQuantity)
                    .setter(OrderItem::setQuantity))
            .build();

    public static final TableSchema<SRTOrder> ORDER_TABLE_SCHEMA = StaticTableSchema.builder(SRTOrder.class)
            .newItemSupplier(SRTOrder::new)
            .addAttribute(String.class, a -> a.name("pk")
                    .getter(SRTOrder::getPk)
                    .setter(SRTOrder::setPk)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
            .addAttribute(String.class, a -> a.name("sk")
                    .getter(SRTOrder::getSk)
                    .setter(SRTOrder::setSk)
                    .tags(StaticAttributeTags.primarySortKey()))
            .addAttribute(String.class, a -> a.name("orderNumber")
                    .getter(SRTOrder::getOrderNumber)
                    .setter(SRTOrder::setOrderNumber))
            .addAttribute(String.class, a -> a.name("customerName")
                    .getter(SRTOrder::getCustomerName)
                    .setter(SRTOrder::setCustomerName))
            .addAttribute(String.class, a -> a.name("customerEmail")
                    .getter(SRTOrder::getCustomerEmail)
                    .setter(SRTOrder::setCustomerEmail))
            .addAttribute(String.class, a -> a.name("cardLast4")
                    .getter(SRTOrder::getCardLast4)
                    .setter(SRTOrder::setCardLast4))
            .addAttribute(EnhancedType.listOf(EnhancedType.documentOf(OrderItem.class, ORDER_ITEM_SCHEMA)),
                    a -> a.name("items")
                            .getter(SRTOrder::getItems)
                            .setter(SRTOrder::setItems))
            .addAttribute(Double.class, a -> a.name("subtotal")
                    .getter(SRTOrder::getSubtotal)
                    .setter(SRTOrder::setSubtotal))
            .addAttribute(Double.class, a -> a.name("tax")
                    .getter(SRTOrder::getTax)
                    .setter(SRTOrder::setTax))
            .addAttribute(Double.class, a -> a.name("grandTotal")
                    .getter(SRTOrder::getGrandTotal)
                    .setter(SRTOrder::setGrandTotal))
            .addAttribute(String.class, a -> a.name("orderDate")
                    .getter(SRTOrder::getOrderDate)
                    .setter(SRTOrder::setOrderDate))
            .addAttribute(String.class, a -> a.name("orderTime")
                    .getter(SRTOrder::getOrderTime)
                    .setter(SRTOrder::setOrderTime))
            .addAttribute(String.class, a -> a.name("status")
                    .getter(SRTOrder::getStatus)
                    .setter(SRTOrder::setStatus))
            .addAttribute(Integer.class, a -> a.name("entityVersion")
                    .getter(SRTOrder::getEntityVersion)
                    .setter(SRTOrder::setEntityVersion))
            .build();

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
        return enhancedClient.table(tables.table(), DISH_TABLE_SCHEMA);
    }
}
