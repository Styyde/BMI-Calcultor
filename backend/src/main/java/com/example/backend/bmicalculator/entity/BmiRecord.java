package com.example.backend.bmicalculator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bmi_records", indexes = {
        @Index(name = "idx_ip_calculated_at", columnList = "ip_address, calculated_at DESC")
})
public class BmiRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(name = "height_cm", nullable = false)
    private Double heightCm;

    @Column(name = "bmi_value", nullable = false)
    private Double bmiValue;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "calculated_at", nullable = false, updatable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        this.calculatedAt = LocalDateTime.now();
    }

    // Constructeurs
    public BmiRecord() {}

    public BmiRecord(String ipAddress, Double weightKg, Double heightCm,
                     Double bmiValue, String category) {
        this.ipAddress = ipAddress;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.bmiValue = bmiValue;
        this.category = category;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getBmiValue() { return bmiValue; }
    public void setBmiValue(Double bmiValue) { this.bmiValue = bmiValue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}