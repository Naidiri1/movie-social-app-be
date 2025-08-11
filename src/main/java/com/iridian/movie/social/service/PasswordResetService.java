package com.iridian.movie.social.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.iridian.movie.social.model.PasswordResetToken;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.PasswordResetTokenRepository;
import com.iridian.movie.social.repository.UserRepository;

@Service
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    @Value("${app.frontend.base-url}")
    private String frontendBase;

    public PasswordResetService(UserRepository userRepo,
            PasswordResetTokenRepository tokenRepo,
            JavaMailSender mailSender) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.mailSender = mailSender;
    }

    public void requestReset(String emailOrUsername) {
        String q = emailOrUsername == null ? "" : emailOrUsername.trim();
        if (q.isEmpty()) {
            return;
        }

        boolean looksLikeEmail = q.contains("@");
        Optional<User> userOpt = looksLikeEmail
                ? userRepo.findByEmail(q.toLowerCase())
                : userRepo.findByUsername(q.toLowerCase());

        userOpt.ifPresent(user -> {
            String raw = generateRawToken(32);
            String hash = sha256(raw);

            PasswordResetToken prt = new PasswordResetToken(
                    user.getUserId(),
                    hash,
                    Instant.now().plus(30, ChronoUnit.MINUTES)
            );
            tokenRepo.save(prt);

            String link = frontendBase + "/reset-password?token=" + raw;
            sendEmail(user.getEmail(), link);
        });
    }

    public void resetPassword(String rawToken, String newPassword) {
        String hash = sha256(rawToken);
        PasswordResetToken token = tokenRepo.findByTokenHash(hash)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        User user = userRepo.findByUserId(token.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        token.setUsed(true);
        tokenRepo.save(token);

    }

        @Value("${spring.mail.username}") private String from;
    private void sendEmail(String to, String link) {
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setFrom(from);
      msg.setTo(to);
      msg.setSubject("Reset your password");
      msg.setText("Click the link to reset your password (valid 30 minutes):\n" + link);
      mailSender.send(msg);
    }
    private String generateRawToken(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return HexFormat.of().formatHex(buf); 
    }

    private String sha256(String s) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
