package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.dto.CartResponse;

public interface CartService {

    CartResponse getMyCart();

    CartResponse addToCart(Long productId, Integer quantity);

    CartResponse updateCartItem(Long productId, Integer quantity);

    void removeFromCart(Long productId);

    void clearCart();
}