package com.iridian.movie.social.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                          .allowedOrigins(
                                "https://movie-social-app-fe.vercel.app",
                                "https://movie-social-app-fe-9g8c-drg74gc1e-naidiri1s-projects.vercel.app",
                                "https://movie-social-app-fe-*.vercel.app", 
                                "http://localhost:3000"
                        ).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*").
                        allowCredentials(true);

            }
        };
    }
}
