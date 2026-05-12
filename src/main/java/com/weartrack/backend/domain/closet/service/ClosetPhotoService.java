package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.PredictedSectionDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetPhotoCreateResDto;
import com.weartrack.backend.domain.closet.entity.ClosetTemplate;
import com.weartrack.backend.domain.clothes.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClosetPhotoService {

    private final S3StorageService s3StorageService;

    public ClosetPhotoCreateResDto uploadClosetPhoto(Long memberId, Integer templateId, MultipartFile image) {
        ClosetTemplate template = ClosetTemplate.from(templateId);

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            S3StorageService.SavedImage savedImage = s3StorageService.uploadClosetImage(image);
            imageUrl = savedImage.getImageUrl();
        }

        List<PredictedSectionDto> predictedSections = template.getSectionOrders()
                .stream()
                .map(order -> new PredictedSectionDto(order, "칸" + order))
                .toList();

        return new ClosetPhotoCreateResDto(
                "SUCCESS",
                template.getTemplateId(),
                imageUrl,
                template.getSectionCount(),
                predictedSections
        );
    }
}