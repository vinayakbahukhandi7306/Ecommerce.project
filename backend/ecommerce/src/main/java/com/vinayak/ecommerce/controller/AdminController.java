package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.dto.AdminDashboardResponse;
import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.security.SecurityService;
import com.vinayak.ecommerce.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vinayak.ecommerce.dto.AdminCustomerResponse;
import java.util.List;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SecurityService securityService;
    private final AdminService adminService;

    public AdminController(
            SecurityService securityService,
            AdminService adminService
    ) {
        this.securityService = securityService;
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/me")
    public String getCurrentUser() {

        User user = securityService.getCurrentUser();

        return "Logged in as: " + user.getEmail();
    }
    @GetMapping("/customers")
    public List<AdminCustomerResponse> getCustomers() {
        return adminService.getCustomers();
    }
}