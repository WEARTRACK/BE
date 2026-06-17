package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.closet.entity.Closet;
import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.domain.closet.repository.ClosetSectionRepository;
import com.weartrack.backend.domain.clothes.dto.request.ClothesCreateRequest;
import com.weartrack.backend.domain.clothes.dto.request.ClothesUpdateRequest;
import com.weartrack.backend.domain.clothes.dto.response.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.dto.response.ClothesDetailResDto;
import com.weartrack.backend.domain.clothes.dto.response.ClothesFilterResDto;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import com.weartrack.backend.domain.clothes.entity.ImageStorageType;
import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.onboarding.entity.QuestType;
import com.weartrack.backend.domain.onboarding.service.OnboardingService;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesService {

    private final ClothesRepository clothesRepository;
    private final ClothesPhotoRepository clothesPhotoRepository;
    private final ClosetSectionRepository closetSectionRepository;
    private final S3StorageService s3StorageService;
    private final OnboardingService onboardingService;

    @Transactional
    public ClothesCreateResponse createClothes(Long memberId, ClothesCreateRequest request) {
        ClothesPhoto clothesPhoto = clothesPhotoRepository.findById(request.photoId())
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_PHOTO_NOT_FOUND));

        if (!clothesPhoto.getMemberId().equals(memberId)) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_PHOTO_NOT_OWNED);
        }

        ClosetSection section = closetSectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!section.getCloset().getMemberId().equals(memberId)) {
            throw new GeneralException(ClosetErrorCode.SECTION_NOT_OWNED);
        }

        Clothes clothes = Clothes.builder()
                .clothesPhotoId(clothesPhoto.getId())
                .closetSectionId(section.getSectionId())
                .imageUrl(clothesPhoto.getImageUrl())
                .color(request.color())
                .category(request.category())
                .price(request.price())
                .build();

        Clothes savedClothes = clothesRepository.save(clothes);
        section.increaseClothesCount();

        updateOnboardingQuestByCategory(memberId, savedClothes.getCategory());

        return toCreateResponse(savedClothes);
    }

    public ClothesFilterResDto filterClothes(
            Long memberId, String color, String category, Pageable pageable
    ) {
        Page<Clothes> page = clothesRepository.searchByMemberIdAndFilters(
                memberId, color, category, pageable
        );

        List<Long> sectionIds = page.getContent().stream()
                .map(Clothes::getClosetSectionId)
                .distinct()
                .toList();

        Map<Long, String> sectionNameMap = closetSectionRepository.findAllById(sectionIds)
                .stream()
                .collect(Collectors.toMap(
                        ClosetSection::getSectionId,
                        ClosetSection::getSectionName
                ));

        return ClothesFilterResDto.from(page, sectionNameMap);
    }

    public ClothesDetailResDto getClothesDetail(Long memberId, Long clothesId) {
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_NOT_FOUND));

        ClosetSection section = closetSectionRepository.findById(clothes.getClosetSectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!section.getCloset().getMemberId().equals(memberId)) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_NOT_OWNED);
        }

        return ClothesDetailResDto.from(clothes, section);
    }

    @Transactional
    public ClothesDetailResDto updateClothes(
            Long memberId, Long clothesId, ClothesUpdateRequest request
    ) {
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_NOT_FOUND));

        ClosetSection currentSection = closetSectionRepository.findById(clothes.getClosetSectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        Closet closet = currentSection.getCloset();

        if (!closet.getMemberId().equals(memberId)) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_NOT_OWNED);
        }

        clothes.updatePrice(request.price());

        ClosetSection finalSection = currentSection;

        if (request.sectionId() != null && !request.sectionId().equals(currentSection.getSectionId())) {
            finalSection = moveClothesToSection(clothes, currentSection, request.sectionId(), memberId);
        }

        return ClothesDetailResDto.from(clothes, finalSection);
    }

    private ClosetSection moveClothesToSection(
            Clothes clothes,
            ClosetSection currentSection,
            Long targetSectionId,
            Long memberId
    ) {
        ClosetSection targetSection = closetSectionRepository.findById(targetSectionId)
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!targetSection.getCloset().getMemberId().equals(memberId)) {
            throw new GeneralException(ClosetErrorCode.SECTION_NOT_OWNED);
        }

        currentSection.decreaseClothesCount();
        targetSection.increaseClothesCount();
        clothes.moveToSection(targetSectionId);

        return targetSection;
    }

    @Transactional
    public void deleteClothes(Long memberId, Long clothesId) {
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_NOT_FOUND));

        ClosetSection section = closetSectionRepository.findById(clothes.getClosetSectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!section.getCloset().getMemberId().equals(memberId)) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_NOT_OWNED);
        }

        clothesPhotoRepository.findById(clothes.getClothesPhotoId())
                .filter(photo -> photo.getImageStorageType() == ImageStorageType.USER_UPLOAD)
                .ifPresent(photo -> {
                    try {
                        s3StorageService.deleteByUrl(photo.getImageUrl());
                    } catch (Exception e) {
                        log.warn("S3 image delete failed. clothesId={}, imageUrl={}",
                                clothesId, photo.getImageUrl(), e);
                    }
                });

        clothesRepository.delete(clothes);
        section.decreaseClothesCount();
    }

    private ClothesCreateResponse toCreateResponse(Clothes savedClothes) {
        return new ClothesCreateResponse(
                savedClothes.getId(),
                savedClothes.getClothesPhotoId(),
                savedClothes.getImageUrl(),
                savedClothes.getProductName(),
                savedClothes.getColor(),
                savedClothes.getCategory(),
                savedClothes.getPrice(),
                savedClothes.getPurchaseDate(),
                savedClothes.getStorageLocation(),
                savedClothes.getClosetSectionId(),
                savedClothes.getCreatedAt()
        );
    }

    private void updateOnboardingQuestByCategory(Long memberId, String category) {
        if (category == null || category.isBlank()) {
            return;
        }

        String normalizedCategory = category.trim().toLowerCase();

        if (isTopCategory(normalizedCategory)) {
            onboardingService.completeQuest(memberId, QuestType.REGISTER_TOP, 1);
            return;
        }

        if (isBottomCategory(normalizedCategory)) {
            onboardingService.completeQuest(memberId, QuestType.REGISTER_BOTTOM, 1);
        }
    }

    private boolean isTopCategory(String category) {
        return List.of(
                "t-shirt",
                "shirt",
                "knit",
                "hoodie",
                "cardigan",
                "jacket",
                "coat",
                "padding",
                "vest"
        ).contains(category);
    }

    private boolean isBottomCategory(String category) {
        return List.of(
                "pants",
                "shorts",
                "skirt"
        ).contains(category);
    }
}