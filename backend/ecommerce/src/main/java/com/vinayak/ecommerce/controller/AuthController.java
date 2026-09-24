package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.dto.AuthResponse;
import com.vinayak.ecommerce.dto.LoginRequest;
import com.vinayak.ecommerce.dto.RegisterRequest;
import com.vinayak.ecommerce.dto.VerifyOtpRequest;
import com.vinayak.ecommerce.service.OtpService;
import com.vinayak.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;

    public AuthController(
            UserService userService,
            OtpService otpService
    ) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        userService.register(request);

        return new ResponseEntity<>(
                "OTP sent to your email",
                HttpStatus.CREATED
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        boolean verified = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (!verified) {
            return ResponseEntity.badRequest()
                    .body("Invalid or expired OTP");
        }

        userService.verifyEmail(request.getEmail());

        return ResponseEntity.ok(
                "Email verified successfully"
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }
}