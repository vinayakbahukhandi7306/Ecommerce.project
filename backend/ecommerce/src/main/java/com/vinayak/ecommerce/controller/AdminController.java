package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.security.SecurityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SecurityService securityService;

    public AdminController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard";
    }

    @GetMapping("/me")
    public String getCurrentUser() {

        User user = securityService.getCurrentUser();

        return "Logged in as: " + user.getEmail();
    }
}