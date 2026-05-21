package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.response.AiClothesPredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ClothesAiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    public AiClothesPredictionResponse predict(byte[] imageBytes, String originalFilename, String contentType) {
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                if (originalFilename == null || originalFilename.isBlank()) {
                    return "clothes-image.jpg";
                }
                return originalFilename;
            }
        };

        MediaType imageMediaType = resolveMediaType(contentType);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder
                .part("file", imageResource)
                .filename(imageResource.getFilename())
                .contentType(imageMediaType);

        return webClientBuilder
                .baseUrl(aiBaseUrl)
                .build()
                .post()
                .uri("/predict-clothes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(AiClothesPredictionResponse.class)
                .block(Duration.ofSeconds(60));
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}