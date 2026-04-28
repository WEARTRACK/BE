package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.ClothesCreateRequest;
import com.weartrack.backend.domain.clothes.dto.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClothesService {

    private final ClothesRepository clothesRepository;
    private final ClothesPhotoRepository clothesPhotoRepository;

    public ClothesCreateResponse createClothes(Long memberId, ClothesCreateRequest request) {

        ClothesPhoto clothesPhoto = clothesPhotoRepository.findById(request.photoId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옷 사진입니다."));

        if (!clothesPhoto.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 업로드한 옷 사진만 등록할 수 있습니다.");
        }

        Clothes clothes = Clothes.builder()
                .clothesPhotoId(clothesPhoto.getId())
                .closetSectionId(request.sectionId())
                .imageUrl(request.imageUrl())
                .color(request.color())
                .category(request.category())
                .price(request.price())
                .build();

        Clothes savedClothes = clothesRepository.save(clothes);

        return new ClothesCreateResponse(
                savedClothes.getId(),
                savedClothes.getClothesPhotoId(),
                savedClothes.getImageUrl(),
                savedClothes.getColor(),
                savedClothes.getCategory(),
                savedClothes.getPrice(),
                savedClothes.getClosetSectionId(),
                savedClothes.getCreatedAt()
        );
    }
}