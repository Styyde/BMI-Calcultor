package com.example.backend.bmicalculator.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BmiCategoryTest {

    @ParameterizedTest(name = "bmi={0} -> {1}")
    @CsvSource({
            "10.0, UNDERWEIGHT",
            "18.4, UNDERWEIGHT",
            "18.5, NORMAL",
            "24.9, NORMAL",
            "25.0, OVERWEIGHT",
            "29.9, OVERWEIGHT",
            "30.0, OBESE_CLASS_1",
            "34.9, OBESE_CLASS_1",
            "35.0, OBESE_CLASS_2",
            "39.9, OBESE_CLASS_2",
            "40.0, OBESE_CLASS_3",
            "60.0, OBESE_CLASS_3"
    })
    void determineCategory_returnsExpectedBracket(double bmi, String expected) {
        assertThat(BmiCategory.determineCategory(bmi)).isEqualTo(BmiCategory.valueOf(expected));
    }

    @Test
    void calculateMinAndMaxIdealWeight_forOneEightyCm() {
        double heightM = 1.80;

        assertThat(BmiCategory.calculateMinIdealWeightKg(heightM)).isEqualTo(59.9);
        assertThat(BmiCategory.calculateMaxIdealWeightKg(heightM)).isEqualTo(80.7);
    }

    @Test
    void poundsToKg_convertsCorrectly() {
        assertThat(BmiCategory.poundsToKg(150)).isEqualTo(68.0);
    }

    @Test
    void feetInchesToCm_convertsCorrectly() {
        assertThat(BmiCategory.feetInchesToCm(5, 10)).isEqualTo(177.8);
    }
}
