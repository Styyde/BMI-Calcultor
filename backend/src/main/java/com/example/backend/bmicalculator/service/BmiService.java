package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.dto.BmiRequest;
import com.example.backend.bmicalculator.dto.BmiResponse;
import com.example.backend.bmicalculator.entity.BmiRecord;
import com.example.backend.bmicalculator.model.BmiCategory;
import com.example.backend.bmicalculator.repository.BmiRepository;
import com.example.backend.bmicalculator.repository.projection.BmiStatsProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BmiService {

    private final BmiRepository bmiRepository;

    public BmiService(BmiRepository bmiRepository) {
        this.bmiRepository = bmiRepository;
    }

    @Transactional
    public BmiResponse calculateAndSave(BmiRequest request, String ipAddress) {

        double heightM = request.getHeight() / 100.0;
        double bmi = round(request.getWeight() / (heightM * heightM));

        BmiCategory category = BmiCategory.determineCategory(bmi);

        double minIdealWeight = BmiCategory.calculateMinIdealWeightKg(heightM);
        double maxIdealWeight = BmiCategory.calculateMaxIdealWeightKg(heightM);

        BmiRecord record = new BmiRecord(
                ipAddress,
                request.getWeight(),
                request.getHeight(),
                bmi,
                category.name()
        );
        bmiRepository.save(record);

        return BmiResponse.builder()
                .weight(request.getWeight())
                .height(request.getHeight())
                .bmi(bmi)
                .category(category.name())
                .label(category.getLabel())
                .advice(category.getAdvice())
                .color(category.getColor())
                .minIdealWeight(minIdealWeight)
                .maxIdealWeight(maxIdealWeight)
                .calculatedAt(record.getCalculatedAt())
                .build();
    }

    @Transactional
    public BmiResponse calculateAndSaveImperial(double weightLbs, int heightFt, int heightIn,
                                                String ipAddress) {
        double weightKg = BmiCategory.poundsToKg(weightLbs);
        double heightCm = BmiCategory.feetInchesToCm(heightFt, heightIn);

        BmiRequest request = new BmiRequest(weightKg, heightCm);
        return calculateAndSave(request, ipAddress);
    }

    public List<BmiResponse> getHistory(String ipAddress, int limit) {
        List<BmiRecord> records = bmiRepository.findTop10ByIpAddressOrderByCalculatedAtDesc(ipAddress);

        return records.stream()
                .limit(limit)
                .map(this::toBmiResponse)
                .collect(Collectors.toList());
    }

    public List<BmiStatsProjection> getStats() {
        return bmiRepository.getCategoryStats();
    }

    private BmiResponse toBmiResponse(BmiRecord record) {
        double heightM = record.getHeightCm() / 100.0;
        BmiCategory category = BmiCategory.valueOf(record.getCategory());

        return BmiResponse.builder()
                .weight(record.getWeightKg())
                .height(record.getHeightCm())
                .bmi(record.getBmiValue())
                .category(category.name())
                .label(category.getLabel())
                .advice(category.getAdvice())
                .color(category.getColor())
                .minIdealWeight(BmiCategory.calculateMinIdealWeightKg(heightM))
                .maxIdealWeight(BmiCategory.calculateMaxIdealWeightKg(heightM))
                .calculatedAt(record.getCalculatedAt())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}