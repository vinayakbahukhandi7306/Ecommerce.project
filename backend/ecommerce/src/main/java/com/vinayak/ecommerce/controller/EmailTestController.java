package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-email")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public String sendTestEmail(@RequestParam String to) {

        emailService.sendEmail(
                to,
                "E-Commerce Email Test",
                "This is a test email from your Spring Boot e-commerce application."
        );

        return "Test email sent successfully";
    }
}