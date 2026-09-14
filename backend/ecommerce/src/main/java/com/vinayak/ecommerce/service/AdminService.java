package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.dto.AdminCustomerResponse;
import com.vinayak.ecommerce.dto.AdminDashboardResponse;

import java.util.List;

public interface AdminService {

    AdminDashboardResponse getDashboard();

    List<AdminCustomerResponse> getCustomers();
}