package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.dto.OrderResponse;
import com.vinayak.ecommerce.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        OrderResponse order =
                orderService.createOrder(productId, quantity);

        return new ResponseEntity<>(
                order,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {

        return ResponseEntity.ok(
                orderService.getMyOrders()
        );
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrderById(orderId)
        );
    }
}