package com.vinayak.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    public OrderResponse() {
    }

    public OrderResponse(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal totalPrice,
            LocalDateTime createdAt) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
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
}