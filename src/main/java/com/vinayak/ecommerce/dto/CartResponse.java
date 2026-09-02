package com.vinayak.ecommerce.dto;

import java.util.List;

public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;

    public CartResponse() {
    }

    public CartResponse(Long cartId, List<CartItemResponse> items) {
        this.cartId = cartId;
        this.items = items;
    }

    public Long getCartId() {
        return cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }
}