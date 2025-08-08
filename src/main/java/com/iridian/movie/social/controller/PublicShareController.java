package com.iridian.movie.social.controller;

import com.iridian.movie.social.dto.Top10Flat;
import com.iridian.movie.social.model.Top10;
import com.iridian.movie.social.repository.Top10Repository;
import com.iridian.movie.social.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/share")
public class PublicShareController {

    private final UserRepository userRepo;
    private final Top10Repository top10Repo;

    public PublicShareController(UserRepository userRepo, Top10Repository top10Repo) {
        this.userRepo = userRepo;
        this.top10Repo = top10Repo;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<List<Top10Flat>> getBySlug(@PathVariable String slug) {
        final UUID shareId;
        try {
            shareId = UUID.fromString(slug);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var user = userRepo.findByShareSlugAndShareEnabledTrue(shareId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var items = top10Repo.findByUser_UserIdOrderByRankAsc(user.getUserId())
                .stream()
                .map(this::toFlatPublic)
                .toList();

        return ResponseEntity.ok(items);
    }

    private Top10Flat toFlatPublic(Top10 t) {
        return new Top10Flat(
                t.getId(),
                null,
                t.getUser().getUsername(), 
                t.getMovieId(),
                t.getTitle(),
                t.getPosterPath(),
                 t.getComment(),
                t.getUserScore(),
                null,
                t.getReleasedDate(),
                t.getRank(),
                t.getMovieDescription(),
                t.getPublicScore(),
                t.getCreatedAt()
        );
    }
}
