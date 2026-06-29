package com.yann.smart_valuator_api.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiEstimationResult {

    private String description;
    private BigDecimal estimatedPrice;

    /** "interesting" if price > €100, "not interesting" otherwise. */
    private String verdict;
}