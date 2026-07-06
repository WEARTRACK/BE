package com.weartrack.backend.domain.closet.dto.response;

import com.weartrack.backend.domain.closet.entity.Closet;
import com.weartrack.backend.domain.closet.entity.ClosetSection;

import java.util.Comparator;
import java.util.List;

public record ClosetInquireResDto(
        Long closetId,
        Integer templateId,
        String closetName,
        String imageUrl,
        Integer sectionCount,
        List<SectionInfo> sections
) {
    public record SectionInfo(
            Long sectionId,
            String sectionName,
            Integer sectionOrder,
            Integer clothesCount
    ) {
        public static SectionInfo from(ClosetSection section) {
            return new SectionInfo(
                    section.getSectionId(),
                    section.getSectionName(),
                    section.getSectionOrder(),
                    section.getClothesCount()
            );
        }
    }

    public static ClosetInquireResDto from(Closet closet) {
        List<SectionInfo> sections = closet.getSections().stream()
                .sorted(Comparator.comparing(ClosetSection::getSectionOrder))
                .map(SectionInfo::from)
                .toList();

        return new ClosetInquireResDto(
                closet.getClosetId(),
                closet.getTemplateId(),
                closet.getClosetName(),
                closet.getImageUrl(),
                sections.size(),
                sections
        );
    }
}