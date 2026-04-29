package com.weartrack.backend.domain.closet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClosetSectionCreateReqDto(
        @NotNull(message = "sectionOrder는 필수입니다.")
        Integer sectionOrder,

        @NotBlank(message = "칸 이름을 모두 입력해주세요.")
        String sectionName
) {
}