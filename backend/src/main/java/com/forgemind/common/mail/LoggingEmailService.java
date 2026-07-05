package com.forgemind.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "app.mail.provider",
        havingValue = "log",
        matchIfMissing = true
)
public class LoggingEmailService implements EmailService {

    @Override
    public void sendEmailVerificationOtp(String to, String otp) {
        log.info("[EMAIL OTP] Verification OTP for {} -> {}", to, otp);
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        log.info("[EMAIL] Verification link for {} -> {}", to, verificationLink);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("[EMAIL] Password reset link for {} -> {}", to, resetLink);
    }
}