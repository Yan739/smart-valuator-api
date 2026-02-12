package com.yann.smart_valuator_api.controller;

import com.yann.smart_valuator_api.entity.Estimation;
import com.yann.smart_valuator_api.service.EstimationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/api/estimations")
public class EstimationController {

    private final EstimationService estimationService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> generateEstimation(@RequestBody Estimation estimation){
        return ResponseEntity.ok(estimationService.generateAiEstimation(estimation));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Estimation>> getAllEstimations(){
        return ResponseEntity.ok(estimationService.getAllEstimations());
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> getEstimationById(@PathVariable Long id){
        return ResponseEntity.ok(estimationService.getEstimationById(id));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> updateEstimation(@PathVariable Long id, @RequestBody Estimation estimation){
        return ResponseEntity.ok(estimationService.updateEstimation(id, estimation));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteEstimation(@PathVariable Long id){
        estimationService.deleteEstimation(id);
        return ResponseEntity.noContent().build();
    }
}


