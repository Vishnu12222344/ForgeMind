package com.forgemind.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Dev-mode implementation. Swap with a real SMTP/SES/SendGrid implementation later
// without touching any calling code (Auth module depends only on the interface).
@Slf4j
@Service
public class LoggingEmailService implements EmailService {

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        log.info("[EMAIL] Verification link for {} -> {}", to, verificationLink);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("[EMAIL] Password reset link for {} -> {}", to, resetLink);
    }
}