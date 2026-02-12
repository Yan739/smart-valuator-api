package com.yann.smart_valuator_api.service;


import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.repository.EstimatimationRepository;
import exception.EstimationNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@AllArgsConstructor


public class EstimationService {

    private final EstimatimationRepository estimationRepository;
    private final HuggingFaceService huggingFaceService;

    public Estimation generateAiEstimation(Estimation estimation) {
        String prompt = String.format(
                "Describe %s, brand %s, category %s, year %d, condition %d/10, " +
                        "give a natural description, approximate price, and advice if interesting.",
                estimation.getItemName(),
                estimation.getBrand(),
                estimation.getCategory(),
                estimation.getYear(),
                estimation.getConditionRating()
        );

        String aiResponse = huggingFaceService.generateDescription(prompt);
        estimation.setAiDescription(aiResponse);

        return estimationRepository.save(estimation);
    }

    public java.util.List<Estimation> getAllEstimations() {
        return estimationRepository.findAll();
    }

    public Estimation getEstimationById(Long id) {
        return estimationRepository.findById(id).orElseThrow(() -> new EstimationNotFoundException(id));
    }


    public void deleteEstimation(Long id) {
        estimationRepository.deleteById(id);
    }

    public Estimation getEstimationByItemName(String itemName) {
        return estimationRepository.findByItemName(itemName);
    }

    public Estimation updateEstimation(Long id, @NonNull Estimation estimation) {
        Estimation existingEstimation = estimationRepository.findById(id).orElseThrow(() -> new EstimationNotFoundException(id));
        existingEstimation.setItemName(estimation.getItemName());
        existingEstimation.setCategory(estimation.getCategory());
        existingEstimation.setBrand(estimation.getBrand());
        existingEstimation.setYear(estimation.getYear());
        existingEstimation.setConditionRating(estimation.getConditionRating());
        existingEstimation.setEstimatedPrice(estimation.getEstimatedPrice());
        existingEstimation.setAiDescription(estimation.getAiDescription());
        existingEstimation.setCreatedAt(estimation.getCreatedAt());
        return estimationRepository.save(existingEstimation);
    }

}
