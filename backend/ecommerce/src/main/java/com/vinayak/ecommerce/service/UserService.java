package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.dto.AuthResponse;
import com.vinayak.ecommerce.dto.LoginRequest;
import com.vinayak.ecommerce.dto.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}