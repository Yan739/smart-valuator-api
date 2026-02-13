package com.yann.smart_valuator_api.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AiEstimationResult {

    @JsonProperty("description")
    private String description;

    @JsonProperty("estimatedPrice")
    private Double estimatedPrice;

    @JsonProperty("verdict")
    private String verdict;
}