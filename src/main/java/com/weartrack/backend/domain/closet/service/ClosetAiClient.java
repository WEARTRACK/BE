package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.response.AiClosetPredictionResponse;
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
public class ClosetAiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url}")
    private String closetAiBaseUrl;

    public AiClosetPredictionResponse predict(byte[] imageBytes, String originalFilename, String contentType) {
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                if (originalFilename == null || originalFilename.isBlank()) {
                    return "closet-image.jpg";
                }
                return originalFilename;
            }
        };

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder
                .part("file", imageResource)
                .filename(imageResource.getFilename())
                .contentType(resolveMediaType(contentType));

        return webClientBuilder
                .baseUrl(closetAiBaseUrl)
                .build()
                .post()
                .uri("/predict-closet")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(AiClosetPredictionResponse.class)
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
