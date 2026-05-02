package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.response.AiClothesPredictionResponse;
import com.weartrack.backend.domain.clothes.dto.response.ClothesPhotoCreateResponse;
import com.weartrack.backend.domain.clothes.dto.ResultDto;
import com.weartrack.backend.domain.clothes.entity.AnalysisStatus;
import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.domain.clothes.util.ColorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClothesPhotoService {

    private final FileStorageService fileStorageService;
    private final ClothesAiClient clothesAiClient;
    private final ClothesPhotoRepository clothesPhotoRepository;

    public ClothesPhotoCreateResponse uploadAndAnalyze(Long memberId, MultipartFile image) {

        FileStorageService.SavedFile savedFile = fileStorageService.save(image);

        try {
            AiClothesPredictionResponse aiResult =
                    clothesAiClient.predict(savedFile.getFile());

            if (aiResult == null || aiResult.results() == null || aiResult.results().isEmpty()) {
                throw new IllegalStateException("AI 분석 결과가 없습니다.");
            }

            List<ResultDto> results = aiResult.results();
            ResultDto firstResult = results.get(0);

            String predictedCategory = firstResult.category();
            String predictedColor = ColorMapper.toEnglish(firstResult.color());

            ClothesPhoto clothesPhoto = ClothesPhoto.builder()
                    .memberId(memberId)
                    .imageUrl(savedFile.getImageUrl())
                    .analysisStatus(AnalysisStatus.SUCCESS)
                    .predictedCategory(predictedCategory)
                    .predictedColor(predictedColor)
                    .build();

            ClothesPhoto savedPhoto = clothesPhotoRepository.save(clothesPhoto);

            return new ClothesPhotoCreateResponse(
                    savedPhoto.getId(),
                    savedPhoto.getImageUrl(),
                    savedPhoto.getAnalysisStatus(),
                    savedPhoto.getPredictedCategory(),
                    savedPhoto.getPredictedColor()
            );

        } catch (Exception e) {
            deleteFileIfExists(savedFile.getFile());
            throw e;
        }
    }

    private void deleteFileIfExists(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}