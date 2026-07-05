package com.forgemind.common.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.mail.provider",
        havingValue = "smtp"
)
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmailVerificationOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Your ForgeMind AI verification code");
        message.setText("""
            Welcome to ForgeMind AI!

            Your email verification code is:

            %s

            This code will expire soon.

            If you did not create a ForgeMind AI account, you can ignore this email.
            """.formatted(otp));

        mailSender.send(message);
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Verify your ForgeMind AI email");
        message.setText("""
            Please verify your email using this link:

            %s
            """.formatted(verificationLink));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Reset your ForgeMind AI password");
        message.setText("""
            You requested a password reset.

            Reset your password using this link:

            %s

            If you did not request this, you can ignore this email.
            """.formatted(resetLink));

        mailSender.send(message);
    }
}