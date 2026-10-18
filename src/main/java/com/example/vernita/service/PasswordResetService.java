
package com.example.vernita.service;

import com.example.vernita.entity.PasswordResetToken;
import com.example.vernita.entity.User;
import com.example.vernita.repository.PasswordResetTokenRepository;
import com.example.vernita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int CODE_EXPIRATION_MINUTES = 10;

    @Transactional
    public void requestPasswordReset(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Don't reveal whether the email exists.
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        // Invalidate previous reset codes for this user.
        passwordResetTokenRepository.deleteAllByUserId(user.getId());

        // Generate a six-digit code.
        String code = String.format(
                "%06d",
                SECURE_RANDOM.nextInt(1_000_000)
        );

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(hashCode(code))
                .expiresAt(
                        LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES)
                )
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendEmail(
                user.getEmail(),
                "Vernita - Password Reset Code",
                "Your password reset code is: " + code
                        + "\n\nThis code expires in 10 minutes."
                        + "\nIf you did not request this, please ignore this email."
        );
    }

    @Transactional
    public void resetPassword(
            String email,
            String code,
            String newPassword
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or expired reset code"
                        )
                );

        String hashedCode = hashCode(code);

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(hashedCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired reset code"
                                )
                        );

        if (!resetToken.getUser().getId().equals(user.getId())
                || resetToken.isUsed()
                || !resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset code"
            );
        }

        // Save the new password using BCrypt.
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Prevent this code from being reused.
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String hashCode(String code) {

        try {
            byte[] secretKey = Base64.getDecoder().decode(jwtSecret);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey,
                    "HmacSHA256"
            );

            mac.init(keySpec);

            byte[] hash = mac.doFinal(
                    code.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Unable to process password reset code",
                    ex
            );
        }
    }
}
