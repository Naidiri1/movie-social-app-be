package com.iridian.movie.social;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.iridian.movie.social.config.JwtConfig;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class MovieSocialAppApplication {

    @Autowired
    private JwtConfig jwtConfig;

    public static void main(String[] args) {
        SpringApplication.run(MovieSocialAppApplication.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("===========================================");
        System.out.println("Movie Social App Started Successfully!");
        System.out.println("JWT Secret Key Loaded: " + (jwtConfig.getSecret() != null ? "Yes" : "No"));
        System.out.println("===========================================");
    }
}