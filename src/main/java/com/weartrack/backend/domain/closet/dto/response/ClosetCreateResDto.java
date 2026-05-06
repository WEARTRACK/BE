package com.weartrack.backend.domain.closet.dto.response;

import java.util.List;

public record ClosetCreateResDto(
        Long closetId,
        Integer templateId,
        String imageUrl,
        List<ClosetSectionResDto> sections
) {
}