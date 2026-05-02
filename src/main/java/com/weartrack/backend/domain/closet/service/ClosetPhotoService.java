package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.response.ClosetPhotoCreateResDto;
import com.weartrack.backend.domain.closet.dto.PredictedSectionDto;
import com.weartrack.backend.domain.closet.entity.ClosetTemplate;
import com.weartrack.backend.domain.clothes.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClosetPhotoService {

    private final FileStorageService fileStorageService;

    public ClosetPhotoCreateResDto uploadClosetPhoto(Long memberId, Integer templateId, MultipartFile image) {
        // TODO: S3 이미지 저장 및 옷장 사진 이력 저장 시 memberId 사용 예정
        ClosetTemplate template = ClosetTemplate.from(templateId);

        FileStorageService.SavedFile savedFile = fileStorageService.saveCloset(image);

        List<PredictedSectionDto> predictedSections = template.getSectionOrders()
                .stream()
                .map(order -> new PredictedSectionDto(order, "칸" + order))
                .toList();

        return new ClosetPhotoCreateResDto(
                "SUCCESS",
                template.getTemplateId(),
                savedFile.getImageUrl(),
                template.getSectionCount(),
                predictedSections
        );
    }
}