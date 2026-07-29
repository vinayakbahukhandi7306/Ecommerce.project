package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.dto.RegisterRequest;
import com.vinayak.ecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {

        userService.register(request);

        return new ResponseEntity<>("User Registered Successfully", HttpStatus.CREATED);
    }
}