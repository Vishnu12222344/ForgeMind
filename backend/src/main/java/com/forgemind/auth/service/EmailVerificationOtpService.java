package com.forgemind.auth.service;

import com.forgemind.auth.entity.EmailVerificationOtp;
import com.forgemind.auth.repository.EmailVerificationOtpRepository;
import com.forgemind.common.exception.BadRequestException;
import com.forgemind.common.mail.EmailService;
import com.forgemind.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationOtpService {

    private final EmailVerificationOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.email-verification.otp-expiration-minutes:10}")
    private long otpExpirationMinutes;

    @Value("${app.email-verification.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    public void createAndSendOtp(User user) {
        otpRepository.deleteByUser_Id(user.getId());

        String otp = generateOtp();

        EmailVerificationOtp entity = EmailVerificationOtp.builder()
                .user(user)
                .otpHash(passwordEncoder.encode(otp))
                .expiryDate(Instant.now().plus(otpExpirationMinutes, ChronoUnit.MINUTES))
                .consumed(false)
                .attempts(0)
                .build();

        otpRepository.save(entity);

        emailService.sendEmailVerificationOtp(user.getEmail(), otp);
    }

    @Transactional
    public void verifyOtp(User user, String otp) {
        EmailVerificationOtp entity = otpRepository
                .findTopByUser_IdAndConsumedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (entity.getExpiryDate().isBefore(Instant.now())) {
            entity.setConsumed(true);
            otpRepository.save(entity);
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }

        if (entity.getAttempts() >= maxAttempts) {
            entity.setConsumed(true);
            otpRepository.save(entity);
            throw new BadRequestException("Too many invalid attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(otp, entity.getOtpHash())) {
            entity.setAttempts(entity.getAttempts() + 1);
            otpRepository.save(entity);
            throw new BadRequestException("Invalid OTP");
        }

        entity.setConsumed(true);
        otpRepository.save(entity);
    }

    private String generateOtp() {
        int number = secureRandom.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}