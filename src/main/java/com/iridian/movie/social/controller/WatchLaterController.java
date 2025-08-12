package com.iridian.movie.social.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.dto.WatchLaterFlat;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.model.WatchLater;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.repository.WatchLaterRepository;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/watch-later")
public class WatchLaterController {

    private final WatchLaterRepository watchLaterRepository;
    private final UserRepository userRepository;

    public WatchLaterController(WatchLaterRepository watchLaterRepository, UserRepository userRepository) {
        this.watchLaterRepository = watchLaterRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<WatchLaterFlat> addWatchLater(@RequestBody WatchLater watchLater,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        watchLater.setUser(user);
        WatchLater saved = watchLaterRepository.save(watchLater);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<WatchLaterFlat>> getWatchLater(@RequestParam String userId) {
        List<WatchLater> watchLater = watchLaterRepository.findByUser_UserId(userId);
        List<WatchLaterFlat> dtos = watchLater.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private WatchLaterFlat convertToFlatDTO(WatchLater w) {
        return new WatchLaterFlat(
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
                GenreMap.toNames(w.getGenreIds()), // Convert genre IDs to names
                w.getCreatedAt()
        );
    }

    @PutMapping("/{watchLaterId}")
    public ResponseEntity<WatchLaterFlat> updateWatchLater(@PathVariable Long watchLaterId,
            @RequestBody WatchLater updatedWatchLater,
            @RequestParam String userId) {
        WatchLater existing = watchLaterRepository.findById(watchLaterId)
                .orElseThrow(() -> new RuntimeException("WatchLater not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedWatchLater.getComment());
        existing.setUserScore(updatedWatchLater.getUserScore());
        existing.setCommentEnabled(updatedWatchLater.getCommentEnabled());

        WatchLater saved = watchLaterRepository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @DeleteMapping("/{watchLaterId}")
    public ResponseEntity<WatchLaterFlat> deleteMovieWatchLater(@PathVariable Long watchLaterId,
            @RequestParam String userId) {
        WatchLater existing = watchLaterRepository.findById(watchLaterId)
                .orElseThrow(() -> new RuntimeException("WatchLater not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        watchLaterRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}