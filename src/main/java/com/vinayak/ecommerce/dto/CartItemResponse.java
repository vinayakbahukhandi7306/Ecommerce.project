package com.vinayak.ecommerce.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;

    public CartItemResponse() {
    }

    public CartItemResponse(
            Long productId,
            String productName,
            BigDecimal price,
            Integer quantity) {

        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }
}