package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.dto.CheckoutResponse;
import com.vinayak.ecommerce.dto.OrderResponse;
import com.vinayak.ecommerce.enums.OrderStatus;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long productId, Integer quantity);

    List<OrderResponse> getMyOrders();

    OrderResponse getOrderById(Long orderId);

    CheckoutResponse checkout();

    List<OrderResponse> getAllOrders();

    void updateOrderStatus(Long orderId, OrderStatus status);
}