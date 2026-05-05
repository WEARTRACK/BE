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
import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
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

    @Transactional
    public ClothesCreateResponse createClothes(Long memberId, ClothesCreateRequest request) {

        ClothesPhoto clothesPhoto = clothesPhotoRepository.findById(request.photoId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옷 사진입니다."));

        if (!clothesPhoto.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 업로드한 옷 사진만 등록할 수 있습니다.");
        }

        ClosetSection section = closetSectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        Clothes clothes = Clothes.builder()
                .clothesPhotoId(clothesPhoto.getId())
                .closetSectionId(request.sectionId())
                .imageUrl(clothesPhoto.getImageUrl())
                .color(request.color())
                .category(request.category())
                .price(request.price())
                .build();

        Clothes savedClothes = clothesRepository.save(clothes);
        section.increaseClothesCount();

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


    // 색상별, 카테고리별 필터링
    public ClothesFilterResDto filterClothes(
            Long memberId, String color, String category, Pageable pageable) {

        Page<Clothes> page = clothesRepository.searchByMemberIdAndFilters(
                memberId, color, category, pageable);

        // 결과에 등장하는 섹션 ID들만 모아서 한 번에 조회 (N+1 방지)
        List<Long> sectionIds = page.getContent().stream()
                .map(Clothes::getClosetSectionId)
                .distinct()
                .toList();

        Map<Long, String> sectionNameMap = closetSectionRepository.findAllById(sectionIds).stream()
                .collect(Collectors.toMap(
                        ClosetSection::getSectionId,
                        ClosetSection::getSectionName
                ));

        return ClothesFilterResDto.from(page, sectionNameMap);
    }


    // 옷 상세정보 조회
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


    // 옷 정보 수정
    @Transactional
    public ClothesDetailResDto updateClothes(
            Long memberId, Long clothesId, ClothesUpdateRequest request) {

        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_NOT_FOUND));

        ClosetSection currentSection = closetSectionRepository.findById(clothes.getClosetSectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        Closet closet = currentSection.getCloset();
        if (!closet.getMemberId().equals(memberId)) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_NOT_OWNED);
        }

        clothes.updateColor(request.color());
        clothes.updateCategory(request.category());
        clothes.updatePrice(request.price());

        ClosetSection finalSection = currentSection;
        if (request.sectionId() != null && !request.sectionId().equals(currentSection.getSectionId())) {
            finalSection = moveClothesToSection(clothes, currentSection, request.sectionId(), memberId);
        }

        return ClothesDetailResDto.from(clothes, finalSection);
    }

    private ClosetSection moveClothesToSection(
            Clothes clothes, ClosetSection currentSection, Long targetSectionId, Long memberId) {

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

    // 옷 정보 삭제
    @Transactional
    public void deleteClothes(Long memberId, Long clothesId) {

        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new GeneralException(ClothesErrorCode.CLOTHES_NOT_FOUND));

        ClosetSection section = closetSectionRepository.findById(clothes.getClosetSectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!section.getCloset().getMemberId().equals(memberId)) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_NOT_OWNED);
        }

        try {
            s3StorageService.deleteByUrl(clothes.getImageUrl());
        } catch (Exception e) {
            log.warn("S3 이미지 삭제 실패. clothesId={}, imageUrl={}",
                    clothesId, clothes.getImageUrl(), e);
        }

        clothesRepository.delete(clothes);
        section.decreaseClothesCount();
    }

}