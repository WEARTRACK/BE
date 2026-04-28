package com.weartrack.backend.domain.clothes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClothesCreateRequest(

        @NotNull(message = "옷 photoId는 필수입니다.")
        Long photoId,

        @NotBlank(message = "색상은 필수입니다.")
        String color,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "옷장 sectionId는 필수입니다.")
        Long sectionId
) {
}
