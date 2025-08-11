package com.iridian.movie.social.service;

import java.time.LocalDate;

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

    public String getPopularMovies() {
    return webClient.get()
            .uri("/movie/popular")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

   public String getUpcomingMovies() {
    LocalDate startDate = LocalDate.of(2025, 8, 11);
    LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(startDate.plusMonths(1).lengthOfMonth());

    return webClient.get()
            .uri(uriBuilder -> uriBuilder
                      .path("/discover/movie")
                    .queryParam("include_adult", "false")
                    .queryParam("include_video", "false")
                    .queryParam("language", "en-US")
                    .queryParam("page", "1")
                    .queryParam("sort_by", "popularity.desc")
                    .queryParam("with_release_type", "2|3") 
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

}
