package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée principal de l'application BMI Calculator.
 * <p>
 * L'annotation {@code @SpringBootApplication} active :
 * <ul>
 *   <li>La configuration automatique Spring Boot</li>
 *   <li>Le scan de composants à partir du package {@code com.example.backend}</li>
 *   <li>La configuration de la classe elle-même comme bean @Configuration</li>
 * </ul>
 */
@SpringBootApplication
public class
BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
