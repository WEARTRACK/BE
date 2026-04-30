package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.AiClothesPredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ClothesAiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    public AiClothesPredictionResponse predict(MultipartFile image) {
        try {
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageResource);

            return webClientBuilder
                    .baseUrl(aiBaseUrl)
                    .build()
                    .post()
                    .uri("/predict-clothes")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(AiClothesPredictionResponse.class)
                    .block();

        } catch (IOException e) {
            throw new IllegalArgumentException("AI 서버로 전달할 이미지 파일을 읽는 중 오류가 발생했습니다.");
        }
    }
}