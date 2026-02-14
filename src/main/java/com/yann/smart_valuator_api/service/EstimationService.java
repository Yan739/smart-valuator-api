package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.repository.EstimationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class EstimationService {

    private final EstimationRepository estimationRepository;
    private final HuggingFaceService huggingFaceService;

    public Estimation generateAiEstimation(Estimation estimation) {
        // Set creation time first
        estimation.setCreatedAt(LocalDateTime.now());

        // Build product details string
        String productDetails = String.format(
                "Item: %s, Brand: %s, Category: %s, Purchase Year: %d, Condition: %d/10",
                estimation.getItemName(),
                estimation.getBrand(),
                estimation.getCategory(),
                estimation.getYear(),
                estimation.getConditionRating()
        );

        try {
            // Call AI service
            AiEstimationResult aiResult =
                    huggingFaceService.generateStructuredEstimation(productDetails);

            // Set AI results
            estimation.setAiDescription(aiResult.getDescription());
            estimation.setEstimatedPrice(aiResult.getEstimatedPrice());

            // Save to database
            Estimation saved = estimationRepository.save(estimation);

            System.out.println("=== SAVED ESTIMATION ===");
            System.out.println("ID: " + saved.getId());
            System.out.println("Price: " + saved.getEstimatedPrice());
            System.out.println("CreatedAt: " + saved.getCreatedAt());
            System.out.println("========================");

            return saved;

        } catch (Exception e) {
            e.printStackTrace();
            // On error, save with error message
            estimation.setAiDescription("Error generating description: " + e.getMessage());
            estimation.setEstimatedPrice(null);
            return estimationRepository.save(estimation);
        }
    }

    public List<Estimation> getAllEstimations() {
        return estimationRepository.findAll();
    }

    public Estimation getEstimationById(Long id) {
        return estimationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimation not found with id: " + id));
    }

    public Estimation updateEstimation(Long id, Estimation estimation) {
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