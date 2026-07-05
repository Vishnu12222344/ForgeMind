package com.forgemind.common.mail;

import com.forgemind.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.mail.provider",
        havingValue = "resend"
)
public class ResendEmailService implements EmailService {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.mail.resend.api-key}")
    private String apiKey;

    @Value("${app.mail.resend.from:ForgeMind AI <onboarding@resend.dev>}")
    private String from;

    private static final String RESEND_BASE_URL = "https://api.resend.com";

    @Override
    public void sendEmailVerificationOtp(String to, String otp) {
        sendEmail(
                to,
                "Your ForgeMind AI verification code",
                """
                Welcome to ForgeMind AI!
    
                Your email verification code is:
    
                %s
    
                This code will expire soon.
    
                If you did not create a ForgeMind AI account, you can ignore this email.
                """.formatted(otp)
        );
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        sendEmail(
                to,
                "Verify your ForgeMind AI email",
                """
                Please verify your email using this link:
    
                %s
                """.formatted(verificationLink)
        );
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        sendEmail(
                to,
                "Reset your ForgeMind AI password",
                """
                You requested a password reset.
    
                Reset your password using this link:
    
                %s
    
                If you did not request this, you can ignore this email.
                """.formatted(resetLink)
        );
    }

    private void sendEmail(String to, String subject, String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Resend API key is not configured");
        }

        RestClient restClient = restClientBuilder
                .baseUrl(RESEND_BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> body = Map.of(
                "from", from,
                "to", List.of(to),
                "subject", subject,
                "text", text
        );

        try {
            restClient
                    .post()
                    .uri("/emails")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new BadRequestException("Email sending failed: " + ex.getMessage());
        }
    }
}