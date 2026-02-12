package com.yann.smart_valuator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

@Service
public class HuggingFaceService {

    @Value("${hf.api.key}")
    private String hfApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateDescription(String inputText) {
        String url = "https://api-inference.huggingface.co/pipeline/text-generation";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + hfApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", inputText);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "Error from Hugging Face API: " + e.getMessage();
        }
    }
}
