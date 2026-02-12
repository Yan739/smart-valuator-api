package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.repository.EstimatimationRepository;
import exception.EstimationNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class EstimationService {

    private final EstimatimationRepository estimationRepository;
    private final HuggingFaceService huggingFaceService;

    public Estimation generateAiEstimation(Estimation estimation) {

        String productDetails = String.format(
                "Item: %s, Brand: %s, Category: %s, Year: %d, Condition: %d/10",
                estimation.getItemName(),
                estimation.getBrand(),
                estimation.getCategory(),
                estimation.getYear(),
                estimation.getConditionRating()
        );

        AiEstimationResult ai = huggingFaceService.generateStructuredEstimation(productDetails);

        estimation.setAiDescription(ai.getDescription());
        estimation.setEstimatedPrice(BigDecimal.valueOf(ai.getEstimatedPrice()));
        estimation.setCreatedAt(LocalDateTime.now());

        return estimationRepository.save(estimation);
    }

    public List<Estimation> getAllEstimations() {
        return estimationRepository.findAll();
    }

    public Estimation getEstimationById(Long id) {
        return estimationRepository.findById(id)
                .orElseThrow(() -> new EstimationNotFoundException(id));
    }

    public void deleteEstimation(Long id) {
        estimationRepository.deleteById(id);
    }

    public Estimation getEstimationByItemName(String itemName) {
        return estimationRepository.findByItemName(itemName);
    }

    public Estimation updateEstimation(Long id, @NonNull Estimation estimation) {
        Estimation existing = estimationRepository.findById(id)
                .orElseThrow(() -> new EstimationNotFoundException(id));

        existing.setItemName(estimation.getItemName());
        existing.setBrand(estimation.getBrand());
        existing.setCategory(estimation.getCategory());
        existing.setYear(estimation.getYear());
        existing.setConditionRating(estimation.getConditionRating());
        existing.setEstimatedPrice(estimation.getEstimatedPrice());
        existing.setAiDescription(estimation.getAiDescription());
        existing.setCreatedAt(estimation.getCreatedAt());

        return estimationRepository.save(existing);
    }
}
