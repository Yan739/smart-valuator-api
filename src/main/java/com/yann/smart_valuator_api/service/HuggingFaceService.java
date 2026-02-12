package com.yann.smart_valuator_api.service;

import com.yann.smart_valuator_api.DTO.AiEstimationResult;
import com.yann.smart_valuator_api.DTO.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

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
            
            Return ONLY a valid JSON like this:
            {
              "description": "...",
              "estimatedPrice": 123.45,
              "verdict": "interesting" | "not interesting"
            }
            
            Product:
            %s
            """.formatted(productDetails);


        ChatCompletionRequest request = new ChatCompletionRequest();
        request.model = "mistralai/Mistral-7B-Instruct-v0.2";
        request.messages = List.of(
                new ChatCompletionRequest.Message("user", prompt)
        );
        request.temperature = 0.7;
        request.max_tokens = 300;

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

        String cleanedJson = rawJson
                .replaceAll("(?s)```.*?\\n", "") // enlève ```json ou ```
                .replaceAll("```", "")
                .trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleanedJson, AiEstimationResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid AI response format: " + rawJson, e);
        }
    }
}
