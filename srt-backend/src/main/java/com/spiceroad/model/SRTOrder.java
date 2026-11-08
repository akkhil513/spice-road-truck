package com.spiceroad.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SRTOrder {

    /** Value of {@code gsiOpen} while an order is not final; the attribute is removed when it closes. */
    public static final String OPEN = "OPEN";

    private String pk;
    private String sk;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String cardLast4;
    private List<OrderItem> items;
    private double subtotal;
    private double tax;
    private double grandTotal;
    private String orderDate;
    private String orderTime;
    private String status;
    private int entityVersion;
    private String source;
    private String orderType;
    private String tableNumber;
    private String paymentStatus;
    private List<Payment> payments;
    private String sentToKitchenAt;
    private Integer orderVersion;
    private String customerPhone;
    private String externalOrderId;
    private String gsiOpen;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbAttribute("orderNumber")
    public String getOrderNumber() { return orderNumber; }

    @DynamoDbAttribute("customerName")
    public String getCustomerName() { return customerName; }

    @DynamoDbAttribute("customerEmail")
    public String getCustomerEmail() { return customerEmail; }

    @DynamoDbAttribute("cardLast4")
    public String getCardLast4() { return cardLast4; }

    @DynamoDbAttribute("items")
    public List<OrderItem> getItems() { return items; }

    @DynamoDbAttribute("subtotal")
    public double getSubtotal() { return subtotal; }

    @DynamoDbAttribute("tax")
    public double getTax() { return tax; }

    @DynamoDbAttribute("grandTotal")
    public double getGrandTotal() { return grandTotal; }

    @DynamoDbAttribute("orderDate")
    public String getOrderDate() { return orderDate; }

    @DynamoDbAttribute("orderTime")
    public String getOrderTime() { return orderTime; }

    @DynamoDbAttribute("status")
    public String getStatus() { return status; }

    @DynamoDbAttribute("entityVersion")
    public int getEntityVersion() { return entityVersion; }

    @DynamoDbAttribute("source")
    public String getSource() { return source; }

    @DynamoDbAttribute("orderType")
    public String getOrderType() { return orderType; }

    @DynamoDbAttribute("tableNumber")
    public String getTableNumber() { return tableNumber; }

    @DynamoDbAttribute("paymentStatus")
    public String getPaymentStatus() { return paymentStatus; }

    @DynamoDbAttribute("payments")
    public List<Payment> getPayments() { return payments; }

    @DynamoDbAttribute("sentToKitchenAt")
    public String getSentToKitchenAt() { return sentToKitchenAt; }

    @DynamoDbAttribute("orderVersion")
    public Integer getOrderVersion() { return orderVersion; }

    @DynamoDbAttribute("customerPhone")
    public String getCustomerPhone() { return customerPhone; }

    @DynamoDbAttribute("externalOrderId")
    public String getExternalOrderId() { return externalOrderId; }

    /** Present only while the order is open; backs the sparse open-index GSI. */
    @DynamoDbAttribute("gsiOpen")
    public String getGsiOpen() { return gsiOpen; }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDbBean
    public static class OrderItem {
        private String id;
        private String name;
        private double price;
        private int quantity;
        private String lineId;
        private String itemStatus;

        @DynamoDbAttribute("id")
        public String getId() { return id; }

        @DynamoDbAttribute("name")
        public String getName() { return name; }

        @DynamoDbAttribute("price")
        public double getPrice() { return price; }

        @DynamoDbAttribute("quantity")
        public int getQuantity() { return quantity; }

        @DynamoDbAttribute("lineId")
        public String getLineId() { return lineId; }

        @DynamoDbAttribute("itemStatus")
        public String getItemStatus() { return itemStatus; }

    }

    /** One money movement on an order. Amounts are whole cents to avoid rounding errors. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDbBean
    public static class Payment {
        private String type;
        private String method;
        private long amountCents;
        private String at;
        private String reason;

        @DynamoDbAttribute("type")
        public String getType() { return type; }

        @DynamoDbAttribute("method")
        public String getMethod() { return method; }

        @DynamoDbAttribute("amountCents")
        public long getAmountCents() { return amountCents; }

        @DynamoDbAttribute("at")
        public String getAt() { return at; }

        @DynamoDbAttribute("reason")
        public String getReason() { return reason; }
    }
}