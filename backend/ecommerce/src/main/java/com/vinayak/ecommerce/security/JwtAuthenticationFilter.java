package com.vinayak.ecommerce.security;

import com.vinayak.ecommerce.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {

                String email = jwtService.extractUsername(token);

                userRepository.findByEmail(email)
                        .ifPresent(user -> {

                            if (jwtService.isTokenValid(token, user)) {

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user,
                                                null,
                                                user.getAuthorities()
                                        );

                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);

                                logger.info(
                                        "User authenticated: {}",
                                        user.getEmail()
                                );
                            }
                        });

            } catch (Exception e) {

                logger.warn(
                        "JWT authentication failed: {} - {}",
                        e.getClass().getSimpleName(),
                        e.getMessage()
                );

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}