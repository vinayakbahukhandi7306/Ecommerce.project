package com.vinayak.ecommerce.dto;

import java.util.List;

public class CheckoutResponse {

    private String message;
    private List<OrderResponse> orders;

    public CheckoutResponse() {
    }

    public CheckoutResponse(
            String message,
            List<OrderResponse> orders) {
        this.message = message;
        this.orders = orders;
    }

    public String getMessage() {
        return message;
    }

    public List<OrderResponse> getOrders() {
        return orders;
    }
}