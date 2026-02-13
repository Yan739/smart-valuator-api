package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.repository.EstimationRepository;
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

    private final EstimationRepository estimationRepository;
    private final HuggingFaceService huggingFaceService;

    public Estimation generateAiEstimation(@NonNull Estimation estimation) {
        String productDetails = String.format(
                "Item: %s, Brand: %s, Category: %s, Purchase Year: %d, Condition: %d/10",
                estimation.getItemName(),
                estimation.getBrand(),
                estimation.getCategory(),
                estimation.getYear(),
                estimation.getConditionRating()
        );

        try {
            AiEstimationResult aiResult =
                    huggingFaceService.generateStructuredEstimation(productDetails);

            estimation.setAiDescription(aiResult.getDescription()); // Cast to BigDecimal
            estimation.setEstimatedPrice(BigDecimal.valueOf(aiResult.getEstimatedPrice()));

            estimation.setCreatedAt(LocalDateTime.now());

            return estimationRepository.save(estimation);

        } catch (Exception e) {
            estimation.setAiDescription("Error generating description: " + e.getMessage());
            estimation.setEstimatedPrice(null);
            estimation.setCreatedAt(LocalDateTime.now());
            return estimationRepository.save(estimation);
        }
    }

    public List<Estimation> getAllEstimations() {
        return estimationRepository.findAll();
    }

    public Estimation getEstimationById(Long id) {
        return estimationRepository.findById(id)
                .orElseThrow(() -> new EstimationNotFoundException(id));
    }

    public Estimation updateEstimation(Long id, @NonNull Estimation estimation) {
        Estimation existing = getEstimationById(id);

        existing.setItemName(estimation.getItemName());
        existing.setBrand(estimation.getBrand());
        existing.setCategory(estimation.getCategory());
        existing.setYear(estimation.getYear());
        existing.setConditionRating(estimation.getConditionRating());
        existing.setEstimatedPrice(estimation.getEstimatedPrice());
        existing.setAiDescription(estimation.getAiDescription());

        return estimationRepository.save(existing);
    }

    public void deleteEstimation(Long id) {
        estimationRepository.deleteById(id);
    }
}