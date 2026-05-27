package com.example.backend.bmicalculator.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor  // ← Important pour le constructeur avec paramètres
public class BmiRequest {

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "20.0", message = "Le poids minimum est de 20 kg")
    @DecimalMax(value = "300.0", message = "Le poids maximum est de 300 kg")
    private Double weight;

    @NotNull(message = "La taille est obligatoire")
    @DecimalMin(value = "100.0", message = "La taille minimum est de 100 cm")
    @DecimalMax(value = "250.0", message = "La taille maximum est de 250 cm")
    private Double height;
}