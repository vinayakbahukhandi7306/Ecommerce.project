package com.vinayak.ecommerce.service.impl;

import com.vinayak.ecommerce.dto.OrderResponse;
import com.vinayak.ecommerce.entity.Order;
import com.vinayak.ecommerce.entity.Product;
import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.repository.OrderRepository;
import com.vinayak.ecommerce.repository.ProductRepository;
import com.vinayak.ecommerce.security.SecurityService;
import com.vinayak.ecommerce.service.OrderService;
import org.springframework.stereotype.Service;
import com.vinayak.ecommerce.exception.OrderAccessDeniedException;
import com.vinayak.ecommerce.exception.ProductNotFoundException;
import com.vinayak.ecommerce.exception.OrderNotFoundException;
import com.vinayak.ecommerce.exception.InvalidQuantityException;
import com.vinayak.ecommerce.exception.InsufficientStockException;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            SecurityService securityService) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.securityService = securityService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long productId, Integer quantity) {

        User currentUser = securityService.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));

        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException(
                    "Quantity must be greater than zero"
            );
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock"
            );
        }

        BigDecimal totalPrice =
                product.getPrice()
                        .multiply(BigDecimal.valueOf(quantity));

        Order order = new Order();

        order.setUser(currentUser);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setCreatedAt(LocalDateTime.now());

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getMyOrders() {

        User currentUser = securityService.getCurrentUser();

        return orderRepository.findByUser(currentUser)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private OrderResponse convertToResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {

        User currentUser = securityService.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new OrderAccessDeniedException(
                    "You are not allowed to access this order"
            );
        }

        return convertToResponse(order);
    }
}