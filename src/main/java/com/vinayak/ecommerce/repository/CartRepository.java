package com.vinayak.ecommerce.repository;

import com.vinayak.ecommerce.entity.Cart;
import com.vinayak.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}