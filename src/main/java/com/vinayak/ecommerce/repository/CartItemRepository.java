package com.vinayak.ecommerce.repository;

import com.vinayak.ecommerce.entity.Cart;
import com.vinayak.ecommerce.entity.CartItem;
import com.vinayak.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}