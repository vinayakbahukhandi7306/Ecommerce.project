package com.vinayak.ecommerce.service.impl;

import com.vinayak.ecommerce.dto.CheckoutResponse;
import com.vinayak.ecommerce.dto.OrderResponse;
import com.vinayak.ecommerce.entity.Cart;
import com.vinayak.ecommerce.entity.CartItem;
import com.vinayak.ecommerce.entity.Order;
import com.vinayak.ecommerce.entity.Product;
import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.enums.OrderStatus;
import com.vinayak.ecommerce.exception.CartNotFoundException;
import com.vinayak.ecommerce.exception.InsufficientStockException;
import com.vinayak.ecommerce.exception.InvalidOrderStatusException;
import com.vinayak.ecommerce.exception.InvalidQuantityException;
import com.vinayak.ecommerce.exception.OrderAccessDeniedException;
import com.vinayak.ecommerce.exception.OrderNotFoundException;
import com.vinayak.ecommerce.exception.ProductNotFoundException;
import com.vinayak.ecommerce.repository.CartRepository;
import com.vinayak.ecommerce.repository.OrderRepository;
import com.vinayak.ecommerce.repository.ProductRepository;
import com.vinayak.ecommerce.security.SecurityService;
import com.vinayak.ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final CartRepository cartRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            SecurityService securityService,
            CartRepository cartRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.cartRepository = cartRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long productId, Integer quantity) {

        User currentUser = securityService.getCurrentUser();

        Product product = productRepository.findByIdWithLock(productId)
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
        order.setStatus(OrderStatus.PLACED);

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        Order savedOrder = orderRepository.save(order);

        logger.info(
                "Order created: orderId={}, userId={}, productId={}, quantity={}",
                savedOrder.getId(),
                currentUser.getId(),
                product.getId(),
                quantity
        );

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

    @Override
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        boolean validTransition =
                (currentStatus == OrderStatus.PLACED &&
                        (status == OrderStatus.CONFIRMED ||
                                status == OrderStatus.CANCELLED))

                        || (currentStatus == OrderStatus.CONFIRMED &&
                        (status == OrderStatus.SHIPPED ||
                                status == OrderStatus.CANCELLED))

                        || (currentStatus == OrderStatus.SHIPPED &&
                        status == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new InvalidOrderStatusException(
                    "Invalid order status transition from "
                            + currentStatus
                            + " to "
                            + status
            );
        }

        order.setStatus(status);

        orderRepository.save(order);

        logger.info(
                "Order status updated: orderId={}, from={}, to={}",
                orderId,
                currentStatus,
                status
        );
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        User currentUser = securityService.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new OrderAccessDeniedException(
                    "You are not allowed to cancel this order"
            );
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new InvalidOrderStatusException(
                    "Only placed orders can be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        Product product = order.getProduct();

        product.setStock(
                product.getStock() + order.getQuantity()
        );

        productRepository.save(product);
        orderRepository.save(order);

        logger.info(
                "Order cancelled: orderId={}, userId={}",
                orderId,
                currentUser.getId()
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

    @Override
    @Transactional
    public CheckoutResponse checkout() {

        User currentUser = securityService.getCurrentUser();

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found"));

        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new InvalidQuantityException("Cart is empty");
        }

        // Lock and validate stock for every item first
        for (CartItem cartItem : cartItems) {

            Product product = productRepository
                    .findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() ->
                            new ProductNotFoundException("Product not found"));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            cartItem.setProduct(product);
        }

        // Create orders and reduce stock
        List<OrderResponse> orderResponses = cartItems.stream()
                .map(cartItem -> {

                    Product product = cartItem.getProduct();
                    Integer quantity = cartItem.getQuantity();

                    BigDecimal totalPrice =
                            product.getPrice()
                                    .multiply(BigDecimal.valueOf(quantity));

                    Order order = new Order();

                    order.setUser(currentUser);
                    order.setProduct(product);
                    order.setQuantity(quantity);
                    order.setTotalPrice(totalPrice);
                    order.setCreatedAt(LocalDateTime.now());
                    order.setStatus(OrderStatus.PLACED);

                    product.setStock(
                            product.getStock() - quantity
                    );

                    productRepository.save(product);

                    Order savedOrder =
                            orderRepository.save(order);

                    return convertToResponse(savedOrder);
                })
                .toList();

        // Clear cart after successful order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        logger.info(
                "Checkout completed: userId={}, ordersCreated={}",
                currentUser.getId(),
                orderResponses.size()
        );

        return new CheckoutResponse(
                "Checkout successful",
                orderResponses
        );
    }

    private OrderResponse convertToResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getStatus()
        );
    }
}