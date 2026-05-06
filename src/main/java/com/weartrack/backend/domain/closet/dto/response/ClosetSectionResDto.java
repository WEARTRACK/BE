package com.weartrack.backend.domain.closet.dto.response;

public record ClosetSectionResDto(
        Long sectionId,
        Integer sectionOrder,
        String sectionName
) {
}