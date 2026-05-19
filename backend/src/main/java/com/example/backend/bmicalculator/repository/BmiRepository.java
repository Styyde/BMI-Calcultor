package com.example.backend.bmicalculator.repository;


import com.example.backend.bmicalculator.entity.BmiRecord;
import com.example.backend.bmicalculator.repository.projection.BmiStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BmiRepository extends JpaRepository<BmiRecord, Long> {

    /**
     * Récupère les 10 derniers calculs pour une adresse IP.
     * Utilisé pour le graphique d'historique utilisateur.
     */
    List<BmiRecord> findTop10ByIpAddressOrderByCalculatedAtDesc(String ipAddress);

    /**
     * Statistiques globales : compte, moyenne, min, max par catégorie.
     * Utilisé pour le graphique en camembert communautaire.
     */
    @Query("SELECT b.category as category, " +
            "COUNT(b) as count, " +
            "AVG(b.bmiValue) as avgBmi, " +
            "MIN(b.bmiValue) as minBmi, " +
            "MAX(b.bmiValue) as maxBmi " +
            "FROM BmiRecord b " +
            "GROUP BY b.category " +
            "ORDER BY COUNT(b) DESC")
    List<BmiStatsProjection> getCategoryStats();
}