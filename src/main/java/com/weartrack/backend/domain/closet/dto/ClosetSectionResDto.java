package com.weartrack.backend.domain.closet.dto;

public record ClosetSectionResDto(
        Long sectionId,
        Integer sectionOrder,
        String sectionName
) {
}