package com.vinayak.ecommerce.service.impl;

import com.vinayak.ecommerce.entity.Otp;
import com.vinayak.ecommerce.repository.OtpRepository;
import com.vinayak.ecommerce.service.EmailService;
import com.vinayak.ecommerce.service.OtpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(
            OtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void generateAndSendOtp(String email) {

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        LocalDateTime now = LocalDateTime.now();

        Otp otpEntity = new Otp();

        otpEntity.setEmail(email);
        otpEntity.setOtpHash(passwordEncoder.encode(otp));
        otpEntity.setCreatedAt(now);
        otpEntity.setExpiresAt(
                now.plusMinutes(OTP_EXPIRY_MINUTES)
        );
        otpEntity.setAttempts(0);
        otpEntity.setUsed(false);

        otpRepository.save(otpEntity);

        emailService.sendEmail(
                email,
                "E-Commerce Email Verification OTP",
                "Your OTP is: " + otp +
                        "\n\nThis OTP will expire in 5 minutes."
        );
    }

    @Override
    @Transactional
    public void resendOtp(String email) {

        Otp latestOtp = otpRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElse(null);

        if (latestOtp != null) {

            LocalDateTime resendAvailableAt =
                    latestOtp.getCreatedAt()
                            .plusSeconds(RESEND_COOLDOWN_SECONDS);

            if (LocalDateTime.now().isBefore(resendAvailableAt)) {
                throw new IllegalStateException(
                        "Please wait before requesting another OTP"
                );
            }
        }

        generateAndSendOtp(email);
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {

        Otp otpEntity = otpRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElse(null);

        if (otpEntity == null) {
            return false;
        }

        if (otpEntity.isUsed()) {
            return false;
        }

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (otpEntity.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        otpEntity.setAttempts(
                otpEntity.getAttempts() + 1
        );

        if (!passwordEncoder.matches(
                otp,
                otpEntity.getOtpHash()
        )) {
            otpRepository.save(otpEntity);
            return false;
        }

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        return true;
    }
}