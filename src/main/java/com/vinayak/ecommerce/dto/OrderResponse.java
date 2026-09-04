package com.vinayak.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.vinayak.ecommerce.enums.OrderStatus;


public class OrderResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private OrderStatus status;

    public OrderResponse() {
    }

    public OrderResponse(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal totalPrice,
            LocalDateTime createdAt,
            OrderStatus status) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }
}