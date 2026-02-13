package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.DTO.ChatCompletionRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HuggingFaceService {

    @Value("${hf.api.key}")
    private String hfApiKey;

    private static final String HF_URL =
            "https://router.huggingface.co/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateDescription(String productDetails) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
            You are an expert in electronics resale.
            
            Analyze this product and return ONLY a valid JSON object (no markdown, no extra text).
            
            JSON format:
            {
              "description": "A natural 2-3 sentence description of the item",
              "estimatedPrice": 123.45,
              "verdict": "interesting"
            }
            
            Rules:
            - description: Natural language description (2-3 sentences)
            - estimatedPrice: Resale price in USD (number or null if unknown)
            - verdict: Either "interesting" or "not interesting"
            - Return ONLY the JSON object, nothing else
            
            Product:
            %s
            """.formatted(productDetails);

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.model = "mistralai/Mistral-7B-Instruct-v0.2";
        request.messages = List.of(
                new ChatCompletionRequest.Message("user", prompt)
        );
        request.temperature = 0.7;
        request.max_tokens = 500;

        HttpEntity<ChatCompletionRequest> entity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(HF_URL, entity, Map.class);

            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) body.get("choices");

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return message.get("content").toString().trim();

        } catch (Exception e) {
            return "Hugging Face error: " + e.getMessage();
        }
    }

    public AiEstimationResult generateStructuredEstimation(String productDetails) {

        String rawJson = generateDescription(productDetails);

        String cleanedJson = extractJsonFromResponse(rawJson);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleanedJson, AiEstimationResult.class);
        } catch (Exception e) {

            System.err.println("Failed to parse AI response: " + rawJson);
            System.err.println("Cleaned JSON: " + cleanedJson);
            System.err.println("Error: " + e.getMessage());

            AiEstimationResult errorResult = new AiEstimationResult();
            errorResult.setDescription("Error parsing AI response: " + e.getMessage());
            errorResult.setEstimatedPrice(null);
            errorResult.setVerdict("not interesting");
            return errorResult;
        }
    }

    private String extractJsonFromResponse(@NonNull String response) {

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

        cleaned = cleaned.replaceAll("^[^{]*", ""); // Remove everything before first {
        cleaned = cleaned.replaceAll("[^}]*$", ""); // Remove everything after last }

        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            return cleaned;
        }

        return cleaned;
    }
}