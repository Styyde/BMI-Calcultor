package com.example.backend.bmicalculator.model;

import java.util.Arrays;

public enum BmiCategory {

    UNDERWEIGHT(
            0.0, 18.5,
            "Insuffisance pondérale",
            "#3498db",
            "Votre poids est insuffisant. Consultez un nutritionniste pour atteindre un poids santé."
    ),

    NORMAL(
            18.5, 25.0,
            "Poids normal",
            "#2ecc71",
            "Excellent ! Continuez à maintenir une alimentation équilibrée et une activité physique régulière."
    ),

    OVERWEIGHT(
            25.0, 30.0,
            "Surpoids",
            "#f39c12",
            "Léger surpoids. Augmentez votre activité physique et surveillez votre alimentation."
    ),

    OBESE_CLASS_1(
            30.0, 35.0,
            "Obésité modérée",
            "#e67e22",
            "Consultez un médecin pour un suivi personnalisé et adoptez de meilleures habitudes de vie."
    ),

    OBESE_CLASS_2(
            35.0, 40.0,
            "Obésité sévère",
            "#e74c3c",
            "Une prise en charge médicale est recommandée pour éviter les complications de santé."
    ),

    OBESE_CLASS_3(
            40.0, Double.MAX_VALUE,
            "Obésité morbide",
            "#c0392b",
            "Consultation médicale urgente nécessaire. Des risques importants pour votre santé sont présents."
    );

    private final double minBmi;
    private final double maxBmi;
    private final String label;
    private final String color;
    private final String advice;

    BmiCategory(double minBmi, double maxBmi, String label, String color, String advice) {
        this.minBmi = minBmi;
        this.maxBmi = maxBmi;
        this.label = label;
        this.color = color;
        this.advice = advice;
    }

    // ==================== GETTERS ====================

    public double getMinBmi() { return minBmi; }
    public double getMaxBmi() { return maxBmi; }
    public String getLabel() { return label; }
    public String getColor() { return color; }
    public String getAdvice() { return advice; }

    // ==================== MÉTHODES STATIQUES ====================

    /**
     * Détermine la catégorie IMC selon la valeur.
     */
    public static BmiCategory determineCategory(double bmi) {
        return Arrays.stream(values())
                .filter(category -> bmi >= category.minBmi && bmi < category.maxBmi)
                .findFirst()
                .orElse(OBESE_CLASS_3);
    }

    /**
     * Calcule le poids idéal minimum (kg) selon la taille en mètres.
     */
    public static double calculateMinIdealWeightKg(double heightM) {
        return round(18.5 * heightM * heightM);
    }

    /**
     * Calcule le poids idéal maximum (kg) selon la taille en mètres.
     */
    public static double calculateMaxIdealWeightKg(double heightM) {
        return round(24.9 * heightM * heightM);
    }

    // ==================== CONVERSIONS ====================

    public static double poundsToKg(double pounds) {
        return round(pounds * 0.453592);
    }

    public static double feetInchesToCm(int feet, int inches) {
        return round((feet * 30.48) + (inches * 2.54));
    }

    // ==================== UTILITAIRE ====================

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}