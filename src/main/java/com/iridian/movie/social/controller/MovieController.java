package com.iridian.movie.social.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.service.TMDBService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final TMDBService tmdbService;

    public MovieController(TMDBService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String query) {
        String result = tmdbService.searchMovies(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularMovies(
            @RequestParam(required = false) Integer with_genres,
            @RequestParam(required = false, defaultValue = "1") Integer page) {
        
        String result;
        
        if (with_genres != null) {
            // Use genre filtering - calls /discover/movie with genre filter
            result = tmdbService.getMoviesByGenre(with_genres, page);
        } else {
            // Return regular popular movies with pagination - calls /movie/popular
            result = tmdbService.getPopularMovies(page);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingMovies() {
        String result = tmdbService.getUpcomingMovies();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movieDetails")
    public ResponseEntity<?> getMovieDetails(@RequestParam Number id) {
        String result = tmdbService.getMovieDetails(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/genres")
    public ResponseEntity<?> getGenres() {
        String result = tmdbService.getGenres();
        return ResponseEntity.ok(result);
    }
}