package com.iridian.movie.social.controller;

import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/share")
public class ShareController {

    private final UserRepository userRepo;

    public ShareController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public record ShareToggle(boolean enabled) {}

    @PutMapping
    public ResponseEntity<Map<String, Object>> toggleShare(@AuthenticationPrincipal Jwt jwt,
                                                           @RequestBody ShareToggle body) {
        if (jwt == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");

        String userId = jwt.getClaimAsString("userId");
        User u = userRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        u.setShareEnabled(body.enabled());
        if (u.getShareSlug() == null && body.enabled()) {
            u.setShareSlug(UUID.randomUUID());
        }
        userRepo.save(u);

        return ResponseEntity.ok(Map.of(
                "enabled", u.getShareEnabled(),
                "slug", u.getShareSlug()     
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getShareStatus(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");

        String userId = jwt.getClaimAsString("userId");
        User u = userRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(Map.of(
                "enabled", u.getShareEnabled(),
                "slug", u.getShareSlug()
        ));
    }
}
