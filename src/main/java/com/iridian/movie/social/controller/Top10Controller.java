package com.iridian.movie.social.controller;

import com.iridian.movie.social.dto.Top10Flat;
import com.iridian.movie.social.model.Top10;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.Top10Repository;
import com.iridian.movie.social.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iridian.movie.social.dto.RankUpdate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/top10")
public class Top10Controller {

    private final Top10Repository top10Repository;
    private final UserRepository userRepository;

    public Top10Controller(Top10Repository top10Repository, UserRepository userRepository) {
        this.top10Repository = top10Repository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Top10Flat> addTop10(@RequestBody Top10 Top10,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Top10.setUser(user);
        Top10 saved = top10Repository.save(Top10);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<Top10Flat>> getTop10(@RequestParam String userId) {
        List<Top10> top10 = top10Repository.findByUser_UserIdOrderByRankAsc(userId);
        List<Top10Flat> dtos = top10.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private Top10Flat convertToFlatDTO(Top10 f) {
        return new Top10Flat(
                f.getId(),
                f.getUser().getUserId(),
                f.getUser().getUsername(),
                f.getMovieId(),
                f.getTitle(),
                f.getPosterPath(),
                f.getComment(),
                f.getUserScore(),
                f.getCommentEnabled(),
                f.getReleasedDate(),
                f.getRank(),
                f.getMovieDescription(),
                f.getPublicScore(),
                f.getCreatedAt());
    }

    @PutMapping("/{top10Id}")
    public ResponseEntity<Top10Flat> updateTop10(@PathVariable Long top10Id,
            @RequestBody Top10 updatedTop10,
            @RequestParam String userId) {
        Top10 existing = top10Repository.findById(top10Id)
                .orElseThrow(() -> new RuntimeException("Top10 not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedTop10.getComment());
        existing.setUserScore(updatedTop10.getUserScore());
        existing.setCommentEnabled(updatedTop10.getCommentEnabled());

        Top10 saved = top10Repository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @PutMapping("/rank")
    public ResponseEntity<?> updateRanks(@RequestBody List<RankUpdate> updates) {
        for (RankUpdate update : updates) {
            Optional<Top10> movieOpt = top10Repository.findById(update.getId());
            movieOpt.ifPresent(movie -> {
                movie.setRank(update.getRank());
                top10Repository.save(movie);
            });
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{top10Id}")
    public ResponseEntity<Top10Flat> deleteMovieTop10(@PathVariable Long top10Id,
            @RequestParam String userId) {
        Top10 existing = top10Repository.findById(top10Id)
                .orElseThrow(() -> new RuntimeException("Top10 not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        top10Repository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
