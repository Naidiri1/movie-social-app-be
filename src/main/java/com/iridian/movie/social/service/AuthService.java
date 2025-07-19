package com.iridian.movie.social.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.util.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

   public User findByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
   }

   public User signup(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);
        return userRepository.save(user);
    }

    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(user.getUserId(), user.getUsername());
    }
}

