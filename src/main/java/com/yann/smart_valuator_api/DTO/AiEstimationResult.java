package com.yann.smart_valuator_api.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiEstimationResult {

    @JsonProperty("description")
    private String description;

    @JsonProperty("estimatedPrice")
    private BigDecimal estimatedPrice;

    @JsonProperty("verdict")
    private String verdict;
}