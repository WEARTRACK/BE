package com.weartrack.backend.domain.closet.dto.response;

import com.weartrack.backend.domain.closet.entity.Closet;
import com.weartrack.backend.domain.closet.entity.ClosetSection;

import java.util.Comparator;
import java.util.List;

public record ClosetSelectResDto(
        Long closetId,
        Integer templateId,
        String imageUrl,
        List<SectionInfo> sections
) {
    public record SectionInfo(
            Long sectionId,
            Integer sectionOrder,
            String sectionName,
            Integer clothesCount
    ) {
        public static SectionInfo from(ClosetSection section) {
            return new SectionInfo(
                    section.getSectionId(),
                    section.getSectionOrder(),
                    section.getSectionName(),
                    section.getClothesCount()
            );
        }
    }

    public static ClosetSelectResDto from(Closet closet) {
        return new ClosetSelectResDto(
                closet.getClosetId(),
                closet.getTemplateId(),
                closet.getImageUrl(),
                closet.getSections().stream()
                        .sorted(Comparator.comparing(ClosetSection::getSectionOrder))
                        .map(SectionInfo::from)
                        .toList()
        );
    }
}