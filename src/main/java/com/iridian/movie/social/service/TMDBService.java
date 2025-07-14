package com.iridian.movie.social.service;

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

    public String searchMovies(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
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
}
