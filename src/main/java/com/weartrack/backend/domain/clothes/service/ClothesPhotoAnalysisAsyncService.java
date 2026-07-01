package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.ResultDto;
import com.weartrack.backend.domain.clothes.dto.response.AiClothesPredictionResponse;
import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.clothes.util.ColorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesPhotoAnalysisAsyncService {

    private final ClothesAiClient clothesAiClient;
    private final ClothesPhotoRepository clothesPhotoRepository;

    @Async("aiAnalysisExecutor")
    @Transactional
    public void analyzeAsync(
            Long photoId,
            byte[] imageBytes,
            String originalFilename,
            String contentType
    ) {
        try {
            AiClothesPredictionResponse aiResult =
                    clothesAiClient.predict(imageBytes, originalFilename, contentType);

            if (aiResult == null || aiResult.results() == null || aiResult.results().isEmpty()) {
                throw new IllegalStateException("AI 분석 결과가 없습니다.");
            }

            List<ResultDto> results = aiResult.results();
            ResultDto firstResult = results.get(0);

            String predictedCategory = CategoryOrder.normalize(firstResult.category());
            String predictedColor = ColorMapper.toEnglish(firstResult.color());

            ClothesPhoto clothesPhoto = clothesPhotoRepository.findById(photoId)
                    .orElseThrow(() -> new IllegalArgumentException("옷 사진 정보를 찾을 수 없습니다. photoId=" + photoId));

            clothesPhoto.markAnalysisSuccess(predictedCategory, predictedColor);

            log.info("옷 사진 AI 분석 성공: photoId={}, category={}, color={}",
                    photoId, predictedCategory, predictedColor);

        } catch (Exception e) {
            log.warn("옷 사진 AI 분석 실패: photoId={}, reason={}", photoId, e.getMessage(), e);

            clothesPhotoRepository.findById(photoId)
                    .ifPresent(ClothesPhoto::markAnalysisFail);
        }
    }
}
