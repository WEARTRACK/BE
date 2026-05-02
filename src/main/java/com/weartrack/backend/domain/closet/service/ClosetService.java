package com.weartrack.backend.domain.closet.service;

import com.weartrack.backend.domain.closet.dto.request.ClosetCreateReqDto;
import com.weartrack.backend.domain.closet.dto.request.ClosetSectionCreateReqDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetCreateResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetInquireResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetSectionResDto;
import com.weartrack.backend.domain.closet.entity.Closet;
import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.closet.entity.ClosetTemplate;
import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.domain.closet.repository.ClosetRepository;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClosetService {

    private final ClosetRepository closetRepository;

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
        Closet closet = closetRepository.findById(closetId).
                orElseThrow(() -> new GeneralException(ClosetErrorCode.CLOSET_NOT_FOUND));

        validateSectionCount(closet);

        return ClosetInquireResDto.from(closet);
    }

    private void validateSectionCount(Closet closet) {
        ClosetTemplate template = ClosetTemplate.from(closet.getTemplateId());
        int actual = closet.getSections().size();
        int expected = template.getSectionCount();

        if (actual != expected) {
          throw new GeneralException(ClosetErrorCode.SECTION_COUNT_MISMATCH);
        }
    }
}