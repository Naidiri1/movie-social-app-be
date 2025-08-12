package com.iridian.movie.social.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.dto.LoginRequest;
import com.iridian.movie.social.dto.SignupRequest;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.service.AuthService;
import com.iridian.movie.social.util.JwtUtil;
import com.iridian.movie.social.util.PrimaryKey;
import com.iridian.movie.social.util.TokenBlackList;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        User user = new User();
        user.setUserId(PrimaryKey.get());
        user.setUsername(request.getUsername().toLowerCase());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());

        User savedUser = authService.signup(user);
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", token);
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("userId", savedUser.getUserId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Collections.singletonMap("access_token", token));
    }

    @GetMapping("/userSession")
    public ResponseEntity<?> getUserSession(@RequestHeader("Authorization") String token) {
    try {
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        if (jwtUtil.isTokenValid(jwt, username)) {
            User user = authService.findByUsername(username);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("userId", user.getUserId());

            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session check failed");
      }
   }

   @Autowired
    private TokenBlackList tokenBlacklist;

    @PostMapping("/logoutUser")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        tokenBlacklist.add(jwt);

        System.out.println("User logged out");

        return ResponseEntity.ok("Logged out");
    }
}
