package com.iridian.movie.social.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.iridian.movie.social.service.PasswordResetService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

  private final PasswordResetService resetService;
  public PasswordResetController(PasswordResetService resetService) { this.resetService = resetService; }

  static class ForgotReq {
    @JsonAlias({"identifier","email","username","emailOrUsername"})
    @NotBlank public String identifier;
  }

  static class ResetReq {
    @NotBlank public String token;       
    @NotBlank public String newPassword; 
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgot(@RequestBody ForgotReq req) {
    resetService.requestReset(req.identifier);
    return ResponseEntity.ok().body(java.util.Map.of("status","ok"));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> reset(@RequestBody ResetReq req) {
    resetService.resetPassword(req.token, req.newPassword);
    return ResponseEntity.ok().body(java.util.Map.of("status","ok"));
  }
}
