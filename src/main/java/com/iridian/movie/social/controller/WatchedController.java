package com.iridian.movie.social.controller;

import com.iridian.movie.social.dto.WatchedFlat;
import com.iridian.movie.social.model.Watched;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.WatchedRepository;
import com.iridian.movie.social.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/watched")
public class WatchedController {

    private final WatchedRepository watchedRepository;
    private final UserRepository userRepository;

    public WatchedController(WatchedRepository watchedRepository, UserRepository userRepository) {
        this.watchedRepository = watchedRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<WatchedFlat> addWatched(@RequestBody Watched watched,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        watched.setUser(user);
        Watched saved = watchedRepository.save(watched);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<WatchedFlat>> getWatched(@RequestParam String userId) {
        List<Watched> watched = watchedRepository.findByUser_UserId(userId);
        List<WatchedFlat> dtos = watched.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private WatchedFlat convertToFlatDTO(Watched w) {
        return new WatchedFlat(
                w.getId(),
                w.getUser().getUserId(),
                w.getUser().getUsername(),
                w.getMovieId(),
                w.getTitle(),
                w.getPosterPath(),
                w.getComment(),
                w.getUserScore(),
                w.getCommentEnabled(),
                w.getReleasedDate(),
                w.getMovieDescription(),
                w.getPublicScore(),
                w.getCreatedAt()

        );
    }

    @PutMapping("/{watchedId}")
    public ResponseEntity<WatchedFlat> updateWatched(@PathVariable Long watchedId,
            @RequestBody Watched updatedWatched,
            @RequestParam String userId) {
        Watched existing = watchedRepository.findById(watchedId)
                .orElseThrow(() -> new RuntimeException("Watched not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedWatched.getComment());
        existing.setUserScore(updatedWatched.getUserScore());
        existing.setCommentEnabled(updatedWatched.getCommentEnabled());

        Watched saved = watchedRepository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @DeleteMapping("/{watchedId}")
    public ResponseEntity<WatchedFlat> deleteMovieWatched(@PathVariable Long watchedId,
            @RequestParam String userId) {
        Watched existing = watchedRepository.findById(watchedId)
                .orElseThrow(() -> new RuntimeException("Watched not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        watchedRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
