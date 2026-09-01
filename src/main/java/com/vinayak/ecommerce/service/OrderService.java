package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.entity.Order;
import com.vinayak.ecommerce.dto.OrderResponse;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long productId, Integer quantity);

    List<OrderResponse> getMyOrders();

    OrderResponse getOrderById(Long orderId);
}