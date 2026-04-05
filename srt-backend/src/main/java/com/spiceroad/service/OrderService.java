package com.spiceroad.service;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.repository.SRTOrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class OrderService {

    @Inject
    SRTOrderRepository orderRepository;

    public SRTOrder placeOrder(SRTOrder order) {
        String orderNumber = orderRepository.getNextOrderNumber();
        order.setPk("ORDER");
        order.setSk(orderNumber);
        order.setOrderNumber(orderNumber);
        order.setOrderDate(LocalDate.now().toString());
        order.setOrderTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        order.setStatus("NEW");
        return orderRepository.saveOrder(order);
    }

    public List<SRTOrder> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    public SRTOrder updateStatus(String sk, String status) {
        return orderRepository.updateStatus(sk, status);
    }
}