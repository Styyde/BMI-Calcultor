package com.example.backend.bmicalculator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration CORS moderne pour BMI Calculator (Java 17+).
 *
 * Approche fonctionnelle avec Set pour éviter les doublons d'origines.
 * Pas de Spring Security - Application 100% publique.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final Set<String> allowedOrigins;

    /**
     * Constructeur avec injection de la propriété configurable.
     *
     * @param origins Chaîne d'origines séparées par des virgules
     */
    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}") String origins) {
        this.allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Configuration CORS simplifiée avec l'API moderne.
     *
     * Points clés :
     * - Pas de credentials (cookie/session) car pas d'authentification
     * - Cache de 3600 secondes pour optimiser les performances
     * - Headers exposés limités au strict nécessaire
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }
}