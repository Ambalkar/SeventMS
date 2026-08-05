package com.eventms.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class GeminiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiService.class);
    private static final String DEFAULT_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    private final String apiKey;
    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.url:" + DEFAULT_API_URL + "}") String apiUrl,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Optional<String> generate(String prompt) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "prompt", Map.of("text", prompt),
                    "temperature", 0.2,
                    "maxOutputTokens", 512,
                    "topP", 0.8,
                    "topK", 40,
                    "candidateCount", 1
            ));

            String requestUrl = apiUrl.contains("?")
                    ? apiUrl + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                    : apiUrl + "?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.warn("Gemini API request failed with status {} and body: {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidateText = root.path("candidates").path(0).path("content").path(0).path("text");
            if (candidateText.isTextual()) {
                return Optional.of(candidateText.asText());
            }

            JsonNode outputText = root.path("output").path(0).path("content").path(0).path("text");
            if (outputText.isTextual()) {
                return Optional.of(outputText.asText());
            }

            LOGGER.warn("Gemini API response did not contain expected text fields: {}", response.body());
            return Optional.empty();
        } catch (IOException exception) {
            LOGGER.error("Failed to call Gemini API", exception);
            return Optional.empty();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.error("Gemini API call was interrupted", exception);
            return Optional.empty();
        }
    }
}
