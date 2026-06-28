package com.yann.smart_valuator_api.DTO;

import tools.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO representing the structured result returned by the AI valuation service.
 * Contains the natural language description, estimated market price in EUR,
 * and a resale verdict for the item.
 */
@Data
public class AiEstimationResult {

    /** Natural language description of the item's condition and market value. */
    @JsonProperty("description")
    private String description;

    /** Estimated resale price in EUR based on AI analysis or fallback pricing. */
    @JsonProperty("estimatedPrice")
    private BigDecimal estimatedPrice;

    /** Resale verdict: "interesting" if price > €100, "not interesting" otherwise. */
    @JsonProperty("verdict")
    private String verdict;
}