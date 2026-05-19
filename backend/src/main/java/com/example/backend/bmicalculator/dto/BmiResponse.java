package com.example.backend.bmicalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BmiResponse {

    private Double weight;
    private Double height;
    private Double bmi;
    private String category;
    private String label;
    private String advice;
    private String color;
    private Double minIdealWeight;
    private Double maxIdealWeight;
    private LocalDateTime calculatedAt;
}