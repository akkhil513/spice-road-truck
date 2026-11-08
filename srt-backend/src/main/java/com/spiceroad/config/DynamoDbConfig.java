package com.spiceroad.config;

import com.spiceroad.model.SRTCustomer;
import com.spiceroad.model.SRTDish;
import com.spiceroad.model.SRTOrder;
import com.spiceroad.model.SRTOrder.OrderItem;
import com.spiceroad.model.SRTOrder.Payment;
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
    private static final TableSchema<Payment> PAYMENT_SCHEMA = StaticTableSchema.builder(Payment.class)
            .newItemSupplier(Payment::new)
            .addAttribute(String.class, a -> a.name("type")
                    .getter(Payment::getType)
                    .setter(Payment::setType))
            .addAttribute(String.class, a -> a.name("method")
                    .getter(Payment::getMethod)
                    .setter(Payment::setMethod))
            .addAttribute(Long.class, a -> a.name("amountCents")
                    .getter(Payment::getAmountCents)
                    .setter(Payment::setAmountCents))
            .addAttribute(String.class, a -> a.name("at")
                    .getter(Payment::getAt)
                    .setter(Payment::setAt))
            .addAttribute(String.class, a -> a.name("reason")
                    .getter(Payment::getReason)
                    .setter(Payment::setReason))
            .build();

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
            .addAttribute(String.class, a -> a.name("lineId")
                    .getter(OrderItem::getLineId)
                    .setter(OrderItem::setLineId))
            .addAttribute(String.class, a -> a.name("itemStatus")
                    .getter(OrderItem::getItemStatus)
                    .setter(OrderItem::setItemStatus))
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
            .addAttribute(String.class, a -> a.name("source")
                    .getter(SRTOrder::getSource)
                    .setter(SRTOrder::setSource))
            .addAttribute(String.class, a -> a.name("orderType")
                    .getter(SRTOrder::getOrderType)
                    .setter(SRTOrder::setOrderType))
            .addAttribute(String.class, a -> a.name("tableNumber")
                    .getter(SRTOrder::getTableNumber)
                    .setter(SRTOrder::setTableNumber))
            .addAttribute(String.class, a -> a.name("paymentStatus")
                    .getter(SRTOrder::getPaymentStatus)
                    .setter(SRTOrder::setPaymentStatus))
            .addAttribute(EnhancedType.listOf(EnhancedType.documentOf(Payment.class, PAYMENT_SCHEMA)),
                    a -> a.name("payments")
                            .getter(SRTOrder::getPayments)
                            .setter(SRTOrder::setPayments))
            .addAttribute(String.class, a -> a.name("sentToKitchenAt")
                    .getter(SRTOrder::getSentToKitchenAt)
                    .setter(SRTOrder::setSentToKitchenAt))
            .addAttribute(Integer.class, a -> a.name("orderVersion")
                    .getter(SRTOrder::getOrderVersion)
                    .setter(SRTOrder::setOrderVersion))
            .addAttribute(String.class, a -> a.name("customerPhone")
                    .getter(SRTOrder::getCustomerPhone)
                    .setter(SRTOrder::setCustomerPhone))
            .addAttribute(String.class, a -> a.name("externalOrderId")
                    .getter(SRTOrder::getExternalOrderId)
                    .setter(SRTOrder::setExternalOrderId))
            .addAttribute(String.class, a -> a.name("gsiOpen")
                    .getter(SRTOrder::getGsiOpen)
                    .setter(SRTOrder::setGsiOpen))
            .build();

    public static final TableSchema<SRTCustomer> CUSTOMER_TABLE_SCHEMA = StaticTableSchema.builder(SRTCustomer.class)
            .newItemSupplier(SRTCustomer::new)
            .addAttribute(String.class, a -> a.name("pk")
                    .getter(SRTCustomer::getPk)
                    .setter(SRTCustomer::setPk)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
            .addAttribute(String.class, a -> a.name("sk")
                    .getter(SRTCustomer::getSk)
                    .setter(SRTCustomer::setSk)
                    .tags(StaticAttributeTags.primarySortKey()))
            .addAttribute(String.class, a -> a.name("name")
                    .getter(SRTCustomer::getName)
                    .setter(SRTCustomer::setName))
            .addAttribute(Boolean.class, a -> a.name("smsOptIn")
                    .getter(SRTCustomer::isSmsOptIn)
                    .setter(SRTCustomer::setSmsOptIn))
            .addAttribute(String.class, a -> a.name("createdAt")
                    .getter(SRTCustomer::getCreatedAt)
                    .setter(SRTCustomer::setCreatedAt))
            .addAttribute(Integer.class, a -> a.name("entityVersion")
                    .getter(SRTCustomer::getEntityVersion)
                    .setter(SRTCustomer::setEntityVersion))
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
