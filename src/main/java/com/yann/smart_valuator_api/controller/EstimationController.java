package com.yann.smart_valuator_api.controller;

import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.service.EstimationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller exposing CRUD endpoints for estimations.
 * Base path: {@code /api/estimations}
 *
 * <p>All endpoints consume and produce {@code application/json}.
 * CORS is enabled globally via {@code @CrossOrigin}.</p>
 */
@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/api/estimations")
public class EstimationController {

    private final EstimationService estimationService;

    /**
     * Creates a new estimation and triggers AI valuation.
     * POST /api/estimations
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> generateEstimation(
            @RequestBody Estimation estimation
    ) {
        return ResponseEntity.ok(
                estimationService.generateAiEstimation(estimation)
        );
    }

    /**
     * Returns the full list of stored estimations.
     * GET /api/estimations
     */
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Estimation>> getAllEstimations() {
        return ResponseEntity.ok(
                estimationService.getAllEstimations()
        );
    }

    /**
     * Returns a single estimation by ID.
     * GET /api/estimations/{id}
     *
     * @param id the estimation ID
     */
    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> getEstimationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                estimationService.getEstimationById(id)
        );
    }

    /**
     * Updates an existing estimation's fields.
     * PUT /api/estimations/{id}
     *
     * @param id         the ID of the estimation to update
     * @param estimation the updated values
     */
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> updateEstimation(
            @PathVariable Long id,
            @RequestBody Estimation estimation
    ) {
        return ResponseEntity.ok(
                estimationService.updateEstimation(id, estimation)
        );
    }

    /**
     * Deletes the estimation with the given ID.
     * DELETE /api/estimations/{id}
     *
     * @param id the estimation ID to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstimation(@PathVariable Long id) {
        estimationService.deleteEstimation(id);
        return ResponseEntity.noContent().build();
    }
}
