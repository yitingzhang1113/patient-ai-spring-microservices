package com.pm.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent}")
    private String geminiApiUrl;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    public Mono<String> getAnswer(String prompt) {
        return webClientBuilder.build()
                .post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(createRequestBody(prompt))
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractContentFromResponse)
                .doOnError(error -> log.error("Error calling Gemini API: {}", error.getMessage()))
                .onErrorReturn("Error: Unable to process request. Please provide default clinical assessment.");
    }
    
    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of(
            "contents", new Object[]{
                Map.of(
                    "parts", new Object[]{
                        Map.of("text", prompt)
                    }
                )
            },
            "generationConfig", Map.of(
                "temperature", 0.3,
                "maxOutputTokens", 2048,
                "topP", 0.8,
                "topK", 40
            )
        );
    }
    
    private String extractContentFromResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidatesNode = rootNode.path("candidates");
            
            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");
                
                if (partsNode.isArray() && partsNode.size() > 0) {
                    return partsNode.get(0).path("text").asText();
                }
            }
            
            log.warn("Unexpected response format from Gemini API: {}", response);
            return "Error: Unexpected response format";
            
        } catch (Exception e) {
            log.error("Error parsing Gemini API response: {}", e.getMessage());
            return "Error: Unable to parse AI response";
        }
    }
}
