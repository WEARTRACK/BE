package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.response.AiClothesPredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ClothesAiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    public AiClothesPredictionResponse predict(File imageFile) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(imageFile));

        return webClientBuilder
                .baseUrl(aiBaseUrl)
                .build()
                .post()
                .uri("/predict-clothes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(AiClothesPredictionResponse.class)
                .timeout(Duration.ofSeconds(40))
                .block();
    }
}