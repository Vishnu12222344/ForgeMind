package com.forgemind.auth.repository;

import com.forgemind.auth.entity.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, UUID> {

    Optional<EmailVerificationOtp> findTopByUser_IdAndConsumedFalseOrderByCreatedAtDesc(UUID userId);

    void deleteByUser_Id(UUID userId);
}