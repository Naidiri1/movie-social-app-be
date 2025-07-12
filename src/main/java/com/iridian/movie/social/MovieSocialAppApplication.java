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
    System.out.println("⚠️ STARTING MAIN METHOD");
    SpringApplication.run(MovieSocialAppApplication.class, args);
    System.out.println("✅ MAIN METHOD COMPLETE"); // should never print — Spring takes over
}

    @PostConstruct
    public void logSecret() {
    System.out.println(">>> MAIN STARTED"); // 👈 should always print
		System.out.println("=== JWT SECRET: " + jwtConfig.getSecret());
    }
}
