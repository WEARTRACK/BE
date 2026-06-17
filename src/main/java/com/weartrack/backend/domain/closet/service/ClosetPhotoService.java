package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.response.AiClosetPredictionResponse;
import com.weartrack.backend.domain.closet.dto.response.ClosetPhotoCreateResDto;
import com.weartrack.backend.domain.closet.dto.response.RecommendedTemplateDto;
import com.weartrack.backend.domain.closet.entity.ClosetTemplate;
import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.domain.clothes.service.S3StorageService;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClosetPhotoService {

    private final S3StorageService s3StorageService;
    private final ClosetAiClient closetAiClient;

    public ClosetPhotoCreateResDto uploadClosetPhoto(Long memberId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new GeneralException(ClosetErrorCode.INVALID_IMAGE);
        }

        byte[] imageBytes = toBytes(image);

        AiClosetPredictionResponse aiResponse = closetAiClient.predict(
                imageBytes,
                image.getOriginalFilename(),
                image.getContentType()
        );

        if (aiResponse == null || aiResponse.recommendedTemplateIds() == null || aiResponse.recommendedTemplateIds().isEmpty()) {
            throw new GeneralException(ClosetErrorCode.INVALID_TEMPLATE_ID);
        }

        List<RecommendedTemplateDto> recommendedTemplates = aiResponse.recommendedTemplateIds()
                .stream()
                .map(templateId -> {
                    ClosetTemplate template = ClosetTemplate.from(templateId);

                    return new RecommendedTemplateDto(
                            template.getTemplateId(),
                            template.getSectionCount()
                    );
                })
                .toList();

        S3StorageService.SavedImage savedImage = s3StorageService.uploadClosetImage(image);

        return new ClosetPhotoCreateResDto(
                "SUCCESS",
                aiResponse.message(),
                savedImage.getImageUrl(),
                aiResponse.detectedSectionCount(),
                recommendedTemplates
        );
    }

    private byte[] toBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new GeneralException(ClosetErrorCode.INVALID_IMAGE);
        }
    }
}