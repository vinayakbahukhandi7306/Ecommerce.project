package com.vinayak.ecommerce.dto;

import com.vinayak.ecommerce.enums.OrderStatus;

public class OrderStatusRequest {

    private OrderStatus status;

    public OrderStatusRequest() {
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}