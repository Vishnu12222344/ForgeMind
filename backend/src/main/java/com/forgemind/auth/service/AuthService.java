package com.forgemind.auth.service;

import com.forgemind.auth.dto.*;
import com.forgemind.auth.entity.PasswordResetToken;
import com.forgemind.auth.entity.RefreshToken;
import com.forgemind.auth.repository.PasswordResetTokenRepository;
import com.forgemind.auth.repository.RefreshTokenRepository;
import com.forgemind.common.exception.BadRequestException;
import com.forgemind.common.exception.ConflictException;
import com.forgemind.common.exception.UnauthorizedException;
import com.forgemind.common.mail.EmailService;
import com.forgemind.security.JwtService;
import com.forgemind.security.UserPrincipal;
import com.forgemind.users.entity.Role;
import com.forgemind.users.entity.User;
import com.forgemind.users.mapper.UserMapper;
import com.forgemind.users.repository.UserRepository;
import com.forgemind.workspaces.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WorkspaceService workspaceService;
    private final EmailService emailService;
    private final EmailVerificationOtpService emailVerificationOtpService;
    @Value("${app.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .emailVerified(false)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        workspaceService.createPersonalWorkspace(user);

        emailVerificationOtpService.createAndSendOtp(user);

        return RegistrationResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .emailVerificationRequired(true)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email is not verified. Please verify your email before logging in.");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired, please login again");
        }

        User user = storedToken.getUser();

        // Rotate refresh token for security
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public AuthResponse verifyEmailOtp(VerifyEmailOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (user.isEmailVerified()) {
            return buildAuthResponse(user);
        }

        emailVerificationOtpService.verifyOtp(user, request.otp());

        user.setEmailVerified(true);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public void resendVerificationOtp(ResendVerificationOtpRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        // Do not reveal whether email exists.
        if (user == null) {
            return;
        }

        if (user.isEmailVerified()) {
            return;
        }

        emailVerificationOtpService.createAndSendOtp(user);
    }

    @Transactional
    public AuthResponse oauthLogin(OAuthUserInfo oauthUserInfo) {
        if (oauthUserInfo.email() == null || oauthUserInfo.email().isBlank()) {
            throw new BadRequestException("OAuth provider did not return an email address");
        }

        User user = userRepository.findByEmail(oauthUserInfo.email()).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(oauthUserInfo.email())
                    .username(generateUniqueUsername(oauthUserInfo))
                    .fullName(resolveFullName(oauthUserInfo))
                    .avatarUrl(oauthUserInfo.avatarUrl())
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.USER)
                    .emailVerified(true)
                    .enabled(true)
                    .build();

            user = userRepository.save(user);

            workspaceService.createPersonalWorkspace(user);
        } else {
            boolean changed = false;

            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                changed = true;
            }

            if (user.getAvatarUrl() == null && oauthUserInfo.avatarUrl() != null) {
                user.setAvatarUrl(oauthUserInfo.avatarUrl());
                changed = true;
            }

            if ((user.getFullName() == null || user.getFullName().isBlank())
                    && oauthUserInfo.name() != null
                    && !oauthUserInfo.name().isBlank()) {
                user.setFullName(oauthUserInfo.name());
                changed = true;
            }

            if (changed) {
                user = userRepository.save(user);
            }
        }

        return buildAuthResponse(user);
    }

    private String resolveFullName(OAuthUserInfo oauthUserInfo) {
        if (oauthUserInfo.name() != null && !oauthUserInfo.name().isBlank()) {
            return oauthUserInfo.name();
        }

        if (oauthUserInfo.username() != null && !oauthUserInfo.username().isBlank()) {
            return oauthUserInfo.username();
        }

        return oauthUserInfo.email();
    }

    private String generateUniqueUsername(OAuthUserInfo oauthUserInfo) {
        String base;

        if (oauthUserInfo.username() != null && !oauthUserInfo.username().isBlank()) {
            base = oauthUserInfo.username();
        } else if (oauthUserInfo.email() != null && oauthUserInfo.email().contains("@")) {
            base = oauthUserInfo.email().substring(0, oauthUserInfo.email().indexOf("@"));
        } else {
            base = "user";
        }

        base = base
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "")
                .toLowerCase();

        if (base.isBlank()) {
            base = "user";
        }

        String username = base;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = base + suffix;
            suffix++;
        }

        return username;
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        // Never reveal whether an email exists in the system
        if (user == null) {
            return;
        }

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(generateSimpleToken())
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                frontendUrl + "/reset-password?token=" + resetToken.getToken()
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all existing sessions for this user
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshTokenValue = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .user(UserMapper.toResponse(user))
                .build();
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID() + UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private String generateSimpleToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}