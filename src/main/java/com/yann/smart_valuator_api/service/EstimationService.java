package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.repository.EstimationRepository;
import exception.EstimationNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for estimation business logic.
 * Orchestrates AI valuation via HuggingFaceService and persistence via EstimationRepository.
 */
@Service
@AllArgsConstructor
public class EstimationService {

    private final EstimationRepository estimationRepository;
    private final HuggingFaceService huggingFaceService;

    /**
     * Generates an AI-powered price estimation for the given item, then persists the result.
     * If the AI call fails, a fallback description and null price are saved instead.
     *
     * @param estimation the item details submitted by the client (must not be null)
     * @return the persisted Estimation with AI description and estimated price
     */
    public Estimation generateAiEstimation(@NonNull Estimation estimation) {
        // Build a structured product string for the AI prompt
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

            estimation.setAiDescription(aiResult.getDescription());
            estimation.setEstimatedPrice(aiResult.getEstimatedPrice());

            Estimation saved = estimationRepository.save(estimation);

            System.out.println("=== SAVED ESTIMATION ===");
            System.out.println("ID: " + saved.getId());
            System.out.println("Price: " + saved.getEstimatedPrice());
            System.out.println("CreatedAt: " + saved.getCreatedAt());
            System.out.println("========================");

            return saved;

        } catch (Exception e) {
            e.printStackTrace();
            // Save with error description and no price so the record is still traceable
            estimation.setAiDescription("Error generating description: " + e.getMessage());
            estimation.setEstimatedPrice(null);
            return estimationRepository.save(estimation);
        }
    }

    /**
     * Returns all estimations stored in the database.
     */
    public List<Estimation> getAllEstimations() {
        return estimationRepository.findAll();
    }

    /**
     * Retrieves a single estimation by its ID.
     *
     * @param id the estimation ID
     * @return the matching Estimation
     * @throws EstimationNotFoundException if no estimation exists with the given ID
     */
    public Estimation getEstimationById(Long id) {
        return estimationRepository.findById(id)
                .orElseThrow(() -> new EstimationNotFoundException(id));
    }

    /**
     * Updates all mutable fields of an existing estimation.
     * The creation timestamp is preserved; only data fields are overwritten.
     *
     * @param id         the ID of the estimation to update
     * @param estimation the new field values (must not be null)
     * @return the updated and persisted Estimation
     * @throws EstimationNotFoundException if no estimation exists with the given ID
     */
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

    /**
     * Deletes the estimation with the given ID.
     *
     * @param id the estimation ID to delete
     */
    public void deleteEstimation(Long id) {
        estimationRepository.deleteById(id);
    }
}