package com.weartrack.backend.domain.closet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ClosetCreateReqDto(
        @NotNull(message = "templateId는 필수입니다.")
        Integer templateId,

        @NotBlank(message = "imageUrl은 필수입니다.")
        String imageUrl,

        @NotEmpty(message = "칸 정보는 필수입니다.")
        List<@NotNull @Valid ClosetSectionCreateReqDto> sections
) {
}