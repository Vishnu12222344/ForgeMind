package com.forgemind.common.mail;

public interface EmailService {

    void sendEmailVerificationOtp(String to, String otp);

    void sendVerificationEmail(String to, String verificationLink);

    void sendPasswordResetEmail(String to, String resetLink);
}