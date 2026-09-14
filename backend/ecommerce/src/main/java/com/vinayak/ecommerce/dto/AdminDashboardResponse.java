package com.vinayak.ecommerce.dto;

import java.math.BigDecimal;

public class AdminDashboardResponse {

    private long totalProducts;
    private long totalOrders;
    private long totalCustomers;
    private BigDecimal totalRevenue;

    public AdminDashboardResponse(
            long totalProducts,
            long totalOrders,
            long totalCustomers,
            BigDecimal totalRevenue
    ) {
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}