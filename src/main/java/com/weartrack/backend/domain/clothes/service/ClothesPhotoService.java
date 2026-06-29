package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.response.ClothesPhotoCreateResponse;
import com.weartrack.backend.domain.clothes.entity.AnalysisStatus;
import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import com.weartrack.backend.domain.clothes.entity.ImageStorageType;
import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ClothesPhotoService {

    private final S3StorageService s3StorageService;
    private final ClothesPhotoRepository clothesPhotoRepository;
    private final ClothesPhotoAnalysisAsyncService clothesPhotoAnalysisAsyncService;

    public ClothesPhotoCreateResponse uploadAndAnalyze(Long memberId, MultipartFile image) {
        s3StorageService.validateImageForUpload(image);

        byte[] imageBytes = toBytes(image);
        String originalFilename = image.getOriginalFilename();
        String contentType = image.getContentType();

        S3StorageService.SavedImage savedImage = s3StorageService.uploadClothesImage(image);

        try {
            ClothesPhoto clothesPhoto = ClothesPhoto.builder()
                    .memberId(memberId)
                    .imageUrl(savedImage.getImageUrl())
                    .imageStorageType(ImageStorageType.USER_UPLOAD)
                    .analysisStatus(AnalysisStatus.PENDING)
                    .predictedCategory(null)
                    .predictedColor(null)
                    .build();

            ClothesPhoto savedPhoto = clothesPhotoRepository.save(clothesPhoto);

            clothesPhotoAnalysisAsyncService.analyzeAsync(
                    savedPhoto.getId(),
                    imageBytes,
                    originalFilename,
                    contentType
            );

            return toResponse(savedPhoto);

        } catch (Exception e) {
            s3StorageService.deleteByKey(savedImage.getKey());
            throw e;
        }
    }

    public ClothesPhotoCreateResponse getAnalysisResult(Long memberId, Long photoId) {
        ClothesPhoto clothesPhoto = clothesPhotoRepository.findByIdAndMemberId(photoId, memberId)
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_PHOTO_NOT_FOUND));

        return toResponse(clothesPhoto);
    }

    private ClothesPhotoCreateResponse toResponse(ClothesPhoto clothesPhoto) {
        return new ClothesPhotoCreateResponse(
                clothesPhoto.getId(),
                clothesPhoto.getImageUrl(),
                clothesPhoto.getAnalysisStatus(),
                clothesPhoto.getPredictedCategory(),
                clothesPhoto.getPredictedColor()
        );
    }

    private byte[] toBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }
}