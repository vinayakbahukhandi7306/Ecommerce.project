package com.vinayak.ecommerce.repository;

import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);
}