package com.example.backend.bmicalculator.repository.projection;

/**
 * Projection pour les statistiques globales par catégorie.
 * Les méthodes correspondent aux alias de la requête JPQL.
 */
public interface BmiStatsProjection {

    String getCategory();
    Long getCount();
    Double getAvgBmi();
    Double getMinBmi();
    Double getMaxBmi();
}