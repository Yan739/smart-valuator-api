package com.yann.smart_valuator_api.DTO;

import lombok.Data;

@Data
public class AiEstimationResult {
    public String description;
    public Double estimatedPrice;
    public String verdict;
}

