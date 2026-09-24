package com.vinayak.ecommerce.repository;

import com.vinayak.ecommerce.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByEmailOrderByIdDesc(String email);
}