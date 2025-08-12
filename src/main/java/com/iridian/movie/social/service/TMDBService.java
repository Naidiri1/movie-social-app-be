package com.iridian.movie.social.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TMDBService {

    private final WebClient webClient;

    public TMDBService(WebClient.Builder webClientBuilder,
            @Value("${tmdb.api.url}") String apiUrl,
            @Value("${tmdb.api.token}") String apiToken) {

        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
    }

    public String searchMovies(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/search/movie")
                .queryParam("query", id)
                .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // Updated getPopularMovies with pagination support and content filtering
    public String getPopularMovies(Integer page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/movie/popular")
                .queryParam("page", page != null ? page : 1)
                .queryParam("certification_country", "US")
                .queryParam("certification.lte", "PG-13") // No R or NC-17 rated movies
                .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // Overloaded method for backward compatibility
    public String getPopularMovies() {
        return getPopularMovies(1);
    }

    public String getUpcomingMovies() {
        ZoneId zone = ZoneId.of("America/New_York");
        LocalDate startDate = LocalDate.now(zone);
        LocalDate endDate = startDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/discover/movie")
                .queryParam("include_adult", "false")
                .queryParam("include_video", "false")
                .queryParam("language", "en-US")
                .queryParam("page", "1")
                 .queryParam("certification_country", "US")
                .queryParam("sort_by", "popularity.desc")
                .queryParam("certification.lte", "PG-13")
                .queryParam("primary_release_date.gte", startDate.toString())
                .queryParam("primary_release_date.lte", endDate.toString())
                .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getMovieDetails(Number id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/movie/{movie_id}")
                .queryParam("id", id)
                .build(id))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getMoviesByGenres(List<Integer> genreIds, Integer page) {
        // Convert list of IDs to comma-separated string (e.g., "28,35")
        String genreIdsStr = genreIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/discover/movie")
                .queryParam("include_adult", "false")
                .queryParam("include_video", "false")
                .queryParam("language", "en-US")
                .queryParam("page", page != null ? page : 1)
                .queryParam("sort_by", "popularity.desc")
                .queryParam("with_genres", genreIdsStr) 
                .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getMoviesByGenres(List<Integer> genreIds) {
        return getMoviesByGenres(genreIds, 1);
    }

    public String getMoviesByGenre(Integer genreId, Integer page) {
        return getMoviesByGenres(List.of(genreId), page);
    }

    public String getGenres() {
        return webClient.get()
                .uri("/genre/movie/list")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}