package com.newwork.employeeprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${app.ai.huggingface.api-key}")
    private String apiKey;

    @Value("${app.ai.huggingface.model}")
    private String model;

    @Value("${app.ai.huggingface.timeout:30s}")
    private Duration timeout;

    @Value("${app.ai.cache.enabled:true}")
    private boolean cacheEnabled;

    @Cacheable(value = "ai-feedback", key = "#content", condition = "#root.target.isCacheEnabled()")
    public String polishFeedback(String content) {
        log.info("Polishing feedback with AI (cache enabled: {})", cacheEnabled);

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("")) {
            log.warn("HuggingFace API key not configured, returning original content");
            return content;
        }

        try {
            String prompt = buildFeedbackPrompt(content);

            WebClient webClient = webClientBuilder
                    .baseUrl(apiUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "inputs", prompt,
                    "parameters", Map.of(
                            "max_length", 500,
                            "temperature", 0.7,
                            "top_p", 0.95
                    )
            );

            String polishedContent = webClient.post()
                    .uri("/" + model)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .onErrorResume(e -> {
                        log.error("Error calling HuggingFace API", e);
                        return Mono.just(content);
                    })
                    .block();

            if (polishedContent != null && !polishedContent.isEmpty()) {
                log.info("Feedback polished successfully");
                return polishedContent;
            }

            return content;
        } catch (Exception e) {
            log.error("Error polishing feedback", e);
            return content;
        }
    }

    private String buildFeedbackPrompt(String content) {
        return "Polish the following professional feedback to make it more constructive, clear, " +
               "and professional while maintaining its core message:\n\n" + content;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
}
