package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.DTO.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HuggingFaceService {

    @Value("${hf.api.key}")
    private String hfApiKey;

    private static final String HF_URL =
            "https://router.huggingface.co/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HuggingFaceService() {
        // Configure RestTemplate with timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 15 seconds
        factory.setReadTimeout(30000);    // 30 seconds
        this.restTemplate = new RestTemplate(factory);
    }

    public String generateDescription(String productDetails) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
            You are an expert in electronics resale valuation for the European market. Provide a realistic market price in Euros (€).
            
            Analyze this used electronics item and return ONLY valid JSON (no markdown):
            
            {
              "description": "Brief description of item condition and market value in Europe",
              "estimatedPrice": 450.00,
              "currency": "EUR",
              "verdict": "interesting"
            }
            
            PRICING GUIDELINES (current used market in EUR - 2026):
            - iPhone 15/15 Pro: 550€ - 850€
            - iPhone 14/14 Pro: 450€ - 650€
            - iPhone 13/13 Pro: 350€ - 500€
            - iPhone 12/12 Pro: 250€ - 380€
            - iPhone 11: 180€ - 280€
            - Samsung Galaxy S23/S24: 350€ - 650€
            - MacBook Air/Pro (M1/M2/M3): 600€ - 1300€
            - iPad Pro (USB-C models): 400€ - 900€
            
            Adjust based on condition rating (multiply by rating/10).
            Take into account battery health if provided.
            
            Product:
            %s
            
            Return ONLY the JSON object with a realistic non-zero price in Euros.
            """.formatted(productDetails);

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.model = "meta-llama/Llama-3.3-70B-Instruct";
        request.messages = List.of(
                new ChatCompletionRequest.Message("user", prompt)
        );
        request.temperature = 0.3;
        request.max_tokens = 500;

        HttpEntity<ChatCompletionRequest> entity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(HF_URL, entity, Map.class);

            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("choices")) {
                return "API returned empty response";
            }

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) body.get("choices");

            if (choices == null || choices.isEmpty()) {
                return "API returned no choices";
            }

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return message.get("content").toString().trim();

        } catch (Exception e) {
            System.err.println("Hugging Face API call failed: " + e.getMessage());
            e.printStackTrace();
            return "API_ERROR";
        }
    }

    public AiEstimationResult generateStructuredEstimation(String productDetails) {

        String rawJson = generateDescription(productDetails);

        System.out.println("=== AI RAW RESPONSE ===");
        System.out.println(rawJson);
        System.out.println("=====================");

        // If API failed, use fallback immediately
        if (rawJson.equals("API_ERROR") || rawJson.startsWith("Hugging Face error") ||
                rawJson.startsWith("API returned")) {
            System.out.println("API call failed, using fallback pricing");
            return createFallbackResult(productDetails);
        }

        String cleanedJson = extractJsonFromResponse(rawJson);

        System.out.println("=== CLEANED JSON ===");
        System.out.println(cleanedJson);
        System.out.println("====================");

        try {
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);

            AiEstimationResult result = new AiEstimationResult();

            // Extract description
            if (jsonNode.has("description") && !jsonNode.get("description").isNull()) {
                result.setDescription(jsonNode.get("description").asText());
            } else {
                result.setDescription(generateFallbackDescription(productDetails));
            }

            BigDecimal price = null;
            if (jsonNode.has("estimatedPrice")) {
                JsonNode priceNode = jsonNode.get("estimatedPrice");
                if (!priceNode.isNull()) {
                    if (priceNode.isNumber()) {
                        price = new BigDecimal(priceNode.asText());
                    } else if (priceNode.isTextual()) {
                        try {
                            String priceText = priceNode.asText().replaceAll("[^0-9.]", "");
                            price = new BigDecimal(priceText);
                        } catch (NumberFormatException e) {
                            price = null;
                        }
                    }
                }
            }

            if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
                System.out.println("WARNING: AI returned zero or null price, using fallback");
                price = estimateFallbackPrice(productDetails);
            }

            result.setEstimatedPrice(price);

            if (jsonNode.has("verdict")) {
                result.setVerdict(jsonNode.get("verdict").asText());
            } else {
                result.setVerdict(price.compareTo(new BigDecimal("100")) > 0 ? "interesting" : "not interesting");
            }

            System.out.println("=== PARSED RESULT ===");
            System.out.println("Description: " + result.getDescription());
            System.out.println("Price: " + result.getEstimatedPrice());
            System.out.println("Verdict: " + result.getVerdict());
            System.out.println("=====================");

            return result;

        } catch (Exception e) {
            System.err.println("Failed to parse AI response, using fallback");
            System.err.println("Error: " + e.getMessage());
            return createFallbackResult(productDetails);
        }
    }

    private AiEstimationResult createFallbackResult(String productDetails) {
        AiEstimationResult result = new AiEstimationResult();
        result.setDescription(generateFallbackDescription(productDetails));
        result.setEstimatedPrice(estimateFallbackPrice(productDetails));
        result.setVerdict(result.getEstimatedPrice().compareTo(new BigDecimal("100")) > 0 ? "interesting" : "not interesting");
        return result;
    }

    private String generateFallbackDescription(String productDetails) {
        // Extract key info
        Pattern namePattern = Pattern.compile("Item: ([^,]+)");
        Pattern conditionPattern = Pattern.compile("Condition: (\\d+)/10");
        Pattern yearPattern = Pattern.compile("Purchase Year: (\\d+)");

        Matcher nameMatcher = namePattern.matcher(productDetails);
        Matcher conditionMatcher = conditionPattern.matcher(productDetails);
        Matcher yearMatcher = yearPattern.matcher(productDetails);

        String itemName = nameMatcher.find() ? nameMatcher.group(1).trim() : "this item";
        String condition = conditionMatcher.find() ? conditionMatcher.group(1) + "/10" : "used";
        String year = yearMatcher.find() ? yearMatcher.group(1) : "unknown year";

        return String.format("A %s from %s in %s condition. Based on current market analysis, " +
                        "this item retains reasonable resale value in the used electronics market.",
                itemName, year, condition);
    }

    private BigDecimal estimateFallbackPrice(String productDetails) {
        String lower = productDetails.toLowerCase();

        double conditionMultiplier = 0.7; // default
        Pattern conditionPattern = Pattern.compile("condition[:\\s]+(\\d+)/10");
        Matcher matcher = conditionPattern.matcher(lower);
        if (matcher.find()) {
            conditionMultiplier = Integer.parseInt(matcher.group(1)) / 10.0;
        }

        BigDecimal basePrice;

        if (lower.contains("iphone")) {
            if (lower.contains("15") || lower.contains("16")) basePrice = new BigDecimal("700");
            else if (lower.contains("14")) basePrice = new BigDecimal("550");
            else if (lower.contains("13")) basePrice = new BigDecimal("400");
            else if (lower.contains("12")) basePrice = new BigDecimal("300");
            else if (lower.contains("11")) basePrice = new BigDecimal("220");
            else if (lower.contains("x") || lower.contains("10")) basePrice = new BigDecimal("180");
            else basePrice = new BigDecimal("150");
        }
        else if (lower.contains("samsung") || lower.contains("galaxy")) {
            if (lower.contains("s24") || lower.contains("s23")) basePrice = new BigDecimal("500");
            else if (lower.contains("s22") || lower.contains("s21")) basePrice = new BigDecimal("350");
            else basePrice = new BigDecimal("200");
        }
        else if (lower.contains("macbook")) {
            if (lower.contains("pro")) basePrice = new BigDecimal("900");
            else basePrice = new BigDecimal("600");
        }
        else if (lower.contains("laptop")) {
            basePrice = new BigDecimal("400");
        }
        else if (lower.contains("ipad")) {
            if (lower.contains("pro")) basePrice = new BigDecimal("500");
            else basePrice = new BigDecimal("300");
        }
        else if (lower.contains("tablet")) {
            basePrice = new BigDecimal("200");
        }
        else if (lower.contains("watch")) basePrice = new BigDecimal("250");
        else if (lower.contains("airpods")) basePrice = new BigDecimal("100");
        else if (lower.contains("console") || lower.contains("playstation") || lower.contains("xbox")) {
            basePrice = new BigDecimal("350");
        }
        else {
            basePrice = new BigDecimal("150");
        }

        BigDecimal adjustedPrice = basePrice.multiply(new BigDecimal(conditionMultiplier));

        return adjustedPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String extractJsonFromResponse(String response) {
        String cleaned = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        Pattern jsonPattern = Pattern.compile(
                "\\{(?:[^{}]|\\{[^{}]*\\})*\\}",
                Pattern.DOTALL
        );
        Matcher matcher = jsonPattern.matcher(cleaned);

        if (matcher.find()) {
            String json = matcher.group().trim();
            int lastBrace = json.lastIndexOf('}');
            if (lastBrace > 0) {
                json = json.substring(0, lastBrace + 1);
            }
            return json;
        }

        cleaned = cleaned.replaceAll("^[^{]*", "");
        cleaned = cleaned.replaceAll("[^}]*$", "");

        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            return cleaned;
        }

        return cleaned;
    }
}