package com.weartrack.backend.domain.closet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ClosetCreateReqDto(
        @NotNull(message = "templateId는 필수입니다.")
        Integer templateId,

        @NotBlank(message = "옷장 이름은 필수입니다.")
        @Size(max = 20, message = "옷장 이름은 20자 이하로 입력해주세요.")
        String closetName,

        String imageUrl,

        @NotEmpty(message = "칸 정보는 필수입니다.")
        List<@NotNull @Valid ClosetSectionCreateReqDto> sections
) {
}