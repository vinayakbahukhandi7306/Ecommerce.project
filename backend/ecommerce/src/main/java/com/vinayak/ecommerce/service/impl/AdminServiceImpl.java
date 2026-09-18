package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.dto.AdminDashboardResponse;
import com.vinayak.ecommerce.entity.Order;
import com.vinayak.ecommerce.enums.OrderStatus;
import com.vinayak.ecommerce.repository.OrderRepository;
import com.vinayak.ecommerce.repository.ProductRepository;
import com.vinayak.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.vinayak.ecommerce.dto.AdminCustomerResponse;
import com.vinayak.ecommerce.enums.Role;
import java.util.List;
import java.math.BigDecimal;

@Service
public class AdminServiceImpl implements AdminService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminServiceImpl(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AdminDashboardResponse getDashboard() {

        long totalProducts = productRepository.count();

        long totalOrders = orderRepository.count();

        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);

        BigDecimal totalRevenue = orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminDashboardResponse(
                totalProducts,
                totalOrders,
                totalCustomers,
                totalRevenue
        );
    }

    @Override
    public List<AdminCustomerResponse> getCustomers() {

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .map(user -> new AdminCustomerResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                ))
                .toList();
    }
}