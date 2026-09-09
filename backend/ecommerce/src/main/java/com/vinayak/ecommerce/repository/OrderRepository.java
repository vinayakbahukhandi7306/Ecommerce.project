package com.vinayak.ecommerce.repository;

import com.vinayak.ecommerce.entity.Order;
import com.vinayak.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}