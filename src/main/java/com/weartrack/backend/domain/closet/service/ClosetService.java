package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.request.ClosetCreateReqDto;
import com.weartrack.backend.domain.closet.dto.request.ClosetSectionCreateReqDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetCreateResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetInquireResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetSectionResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetStatisticsDto;
import com.weartrack.backend.domain.closet.entity.Closet;
import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.closet.entity.ClosetTemplate;
import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.domain.closet.repository.ClosetRepository;
import com.weartrack.backend.domain.closet.repository.ClosetSectionRepository;
import com.weartrack.backend.domain.clothes.dto.response.ClothesListResDto;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClosetService {

    private final ClosetRepository closetRepository;
    private final ClosetSectionRepository sectionRepository;
    private final ClothesRepository clothesRepository;

    public ClosetCreateResDto createCloset(Long memberId, ClosetCreateReqDto request) {
        ClosetTemplate template = ClosetTemplate.from(request.templateId());

        validateSections(template, request.sections());

        Closet closet = Closet.builder()
                .memberId(memberId)
                .templateId(request.templateId())
                .imageUrl(request.imageUrl())
                .build();

        request.sections()
                .stream()
                .sorted(Comparator.comparing(ClosetSectionCreateReqDto::sectionOrder))
                .forEach(sectionRequest -> {
                    ClosetSection section = ClosetSection.builder()
                            .sectionOrder(sectionRequest.sectionOrder())
                            .sectionName(sectionRequest.sectionName())
                            .build();

                    closet.addSection(section);
                });

        Closet savedCloset = closetRepository.save(closet);

        List<ClosetSectionResDto> sectionResponses = savedCloset.getSections()
                .stream()
                .sorted(Comparator.comparing(ClosetSection::getSectionOrder))
                .map(section -> new ClosetSectionResDto(
                        section.getSectionId(),
                        section.getSectionOrder(),
                        section.getSectionName()
                ))
                .toList();

        return new ClosetCreateResDto(
                savedCloset.getClosetId(),
                savedCloset.getTemplateId(),
                savedCloset.getImageUrl(),
                sectionResponses
        );
    }

    private void validateSections(ClosetTemplate template, List<ClosetSectionCreateReqDto> sections) {
        if (sections.size() != template.getSectionCount()) {
            throw new GeneralException(ClosetErrorCode.INVALID_SECTION_COUNT);
        }

        boolean hasEmptyName = sections.stream()
                .anyMatch(section -> section.sectionName() == null || section.sectionName().isBlank());

        if (hasEmptyName) {
            throw new GeneralException(ClosetErrorCode.EMPTY_SECTION_NAME);
        }

        List<Integer> sectionOrders = sections.stream()
                .map(ClosetSectionCreateReqDto::sectionOrder)
                .sorted()
                .toList();

        List<Integer> validOrders = template.getSectionOrders();

        if (!sectionOrders.equals(validOrders)) {
            throw new GeneralException(ClosetErrorCode.INVALID_SECTION_ORDER);
        }
    }

    //내 옷장 조회하는 API
    public ClosetInquireResDto getCloset(Long memberId, Long closetId) {
        Closet closet = findClosetWithOwnershipCheck(memberId, closetId);
        validateSectionCount(closet);
        return ClosetInquireResDto.from(closet);
    }

    //옷장의 특정 칸에 해당하는 옷 리스트를 조회하는 API
    public ClothesListResDto getClothesBySection(Long memberId, Long closetId, Long sectionId, Pageable pageable){

        findClosetWithOwnershipCheck(memberId, closetId);

        ClosetSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!section.getCloset().getClosetId().equals(closetId)) {
            throw new GeneralException(ClosetErrorCode.SECTION_NOT_IN_CLOSET);
        }
        Page<Clothes> page = clothesRepository.findByClosetSectionIdOrderByCreatedAtDesc(sectionId,pageable);

        return ClothesListResDto.from(section, page);
    }

    // 통계 조회하는 API
    public ClosetStatisticsDto getStatistics(Long memberId, Long closetId){
        findClosetWithOwnershipCheck(memberId, closetId);
        List<Clothes> clothesList = clothesRepository.findAllByClosetId(closetId);
        return ClosetStatisticsDto.from(clothesList);
    }

    private Closet findClosetWithOwnershipCheck(Long memberId, Long closetId) {
        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.CLOSET_NOT_FOUND));

        if (!closet.getMemberId().equals(memberId)) {
            throw new GeneralException(ClosetErrorCode.CLOSET_NOT_OWNED);
        }
        return closet;
    }

    private void validateSectionCount(Closet closet) {
        ClosetTemplate template = ClosetTemplate.from(closet.getTemplateId());
        int actual = closet.getSections().size();
        int expected = template.getSectionCount();

        if (actual != expected) {
            log.warn("칸의 수가 불일치합니다. closetId={}, expected={}, actual={}",
                    closet.getClosetId(), expected, actual);
        }
    }
}