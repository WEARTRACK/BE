package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.response.AiClosetPredictionResponse;
import com.weartrack.backend.domain.closet.dto.response.ClosetPhotoCreateResDto;
import com.weartrack.backend.domain.closet.dto.response.RecommendedTemplateDto;
import com.weartrack.backend.domain.closet.entity.ClosetTemplate;
import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.domain.clothes.service.S3StorageService;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosetPhotoService {

    private final S3StorageService s3StorageService;
    private final ClosetAiClient closetAiClient;

    public ClosetPhotoCreateResDto uploadClosetPhoto(Long memberId, MultipartFile image) {
        log.info("[ClosetPhoto] upload start. memberId={}, filename={}, contentType={}, size={}",
                memberId,
                image != null ? image.getOriginalFilename() : null,
                image != null ? image.getContentType() : null,
                image != null ? image.getSize() : null
        );

        if (image == null || image.isEmpty()) {
            log.warn("[ClosetPhoto] invalid image. image is null or empty");
            throw new GeneralException(ClosetErrorCode.INVALID_IMAGE);
        }

        byte[] imageBytes = toBytes(image);

        log.info("[ClosetPhoto] before AI request");

        AiClosetPredictionResponse aiResponse = closetAiClient.predict(
                imageBytes,
                image.getOriginalFilename(),
                image.getContentType()
        );

        log.info("[ClosetPhoto] after AI response. response={}", aiResponse);

        if (aiResponse == null
                || aiResponse.recommendedTemplateIds() == null
                || aiResponse.recommendedTemplateIds().isEmpty()) {
            log.warn("[ClosetPhoto] invalid AI response. response={}", aiResponse);
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

        log.info("[ClosetPhoto] recommendedTemplates={}", recommendedTemplates);
        log.info("[ClosetPhoto] before S3 upload");

        S3StorageService.SavedImage savedImage = s3StorageService.uploadClosetImage(image);

        log.info("[ClosetPhoto] after S3 upload. imageUrl={}", savedImage.getImageUrl());

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
            log.error("[ClosetPhoto] failed to convert image to bytes", e);
            throw new GeneralException(ClosetErrorCode.INVALID_IMAGE);
        }
    }
}