package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.dto.BmiRequest;
import com.example.backend.bmicalculator.dto.BmiResponse;
import com.example.backend.bmicalculator.entity.BmiRecord;
import com.example.backend.bmicalculator.entity.User;
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

    // ✅ Méthode principale avec support utilisateur (peut être null)
    @Transactional
    public BmiResponse calculateAndSave(BmiRequest request, String ipAddress, User user) {

        double heightM = request.getHeight() / 100.0;
        double bmi = round(request.getWeight() / (heightM * heightM));

        BmiCategory category = BmiCategory.determineCategory(bmi);

        double minIdealWeight = BmiCategory.calculateMinIdealWeightKg(heightM);
        double maxIdealWeight = BmiCategory.calculateMaxIdealWeightKg(heightM);

        // ✅ Utiliser le constructeur AVEC user (peut être null)
        BmiRecord record = new BmiRecord(
                user,        // ← NOUVEAU : l'utilisateur connecté (ou null)
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

    // ✅ Version sans user (pour compatibilité, mais à éviter)
    @Transactional
    public BmiResponse calculateAndSave(BmiRequest request, String ipAddress) {
        return calculateAndSave(request, ipAddress, null);
    }

    @Transactional
    public BmiResponse calculateAndSaveImperial(double weightLbs, int heightFt, int heightIn,
                                                String ipAddress, User user) {
        double weightKg = BmiCategory.poundsToKg(weightLbs);
        double heightCm = BmiCategory.feetInchesToCm(heightFt, heightIn);

        BmiRequest request = new BmiRequest(weightKg, heightCm);
        return calculateAndSave(request, ipAddress, user);
    }

    // ✅ Version sans user pour compatibilité
    @Transactional
    public BmiResponse calculateAndSaveImperial(double weightLbs, int heightFt, int heightIn,
                                                String ipAddress) {
        return calculateAndSaveImperial(weightLbs, heightFt, heightIn, ipAddress, null);
    }

    // ✅ Méthode getHistory qui supporte user (priorité sur IP)
    public List<BmiResponse> getHistory(String ipAddress, User user, int limit) {
        List<BmiRecord> records;

        if (user != null) {
            // Utilisateur connecté : on récupère son historique par user_id
            records = bmiRepository.findTop10ByUserIdOrderByCalculatedAtDesc(user.getId());
        } else {
            // Non connecté : fallback sur l'adresse IP
            records = bmiRepository.findTop10ByIpAddressOrderByCalculatedAtDesc(ipAddress);
        }

        return records.stream()
                .limit(limit)
                .map(this::toBmiResponse)
                .collect(Collectors.toList());
    }

    // ✅ Version sans user pour compatibilité
    public List<BmiResponse> getHistory(String ipAddress, int limit) {
        return getHistory(ipAddress, null, limit);
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