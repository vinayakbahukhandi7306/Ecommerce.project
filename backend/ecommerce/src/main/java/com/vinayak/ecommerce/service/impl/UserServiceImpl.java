package com.vinayak.ecommerce.service.impl;

import com.vinayak.ecommerce.dto.AuthResponse;
import com.vinayak.ecommerce.dto.LoginRequest;
import com.vinayak.ecommerce.dto.RegisterRequest;
import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.enums.Role;
import com.vinayak.ecommerce.exception.EmailAlreadyExistsException;
import com.vinayak.ecommerce.repository.UserRepository;
import com.vinayak.ecommerce.security.JwtService;
import com.vinayak.ecommerce.service.OtpService;
import com.vinayak.ecommerce.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OtpService otpService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.CUSTOMER);

        // New users must verify their email first
        user.setEmailVerified(false);

        userRepository.save(user);

        // Generate and send OTP
        otpService.generateAndSendOtp(user.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "Login Successful");
    }

    @Override
    public void verifyEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setEmailVerified(true);

        userRepository.save(user);
    }
}