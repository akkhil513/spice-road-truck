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

    public void setPk(String pk) { this.pk = pk; }

    public void setSk(String sk) { this.sk = sk; }

    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public void setItems(List<OrderItem> items) { this.items = items; }

    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public void setTax(double tax) { this.tax = tax; }

    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public void setStatus(String status) { this.status = status; }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDbBean
    public static class OrderItem {
        private String id;
        private String name;
        private double price;
        private int quantity;

        @DynamoDbAttribute("id")
        public String getId() { return id; }

        @DynamoDbAttribute("name")
        public String getName() { return name; }

        @DynamoDbAttribute("price")
        public double getPrice() { return price; }

        @DynamoDbAttribute("quantity")
        public int getQuantity() { return quantity; }

        public void setId(String id) { this.id = id; }

        public void setName(String name) { this.name = name; }

        public void setPrice(double price) { this.price = price; }

        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}