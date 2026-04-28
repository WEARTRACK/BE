package com.weartrack.backend.domain.clothes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClothesCreateRequest(

        @NotNull(message = "photoId는 필수입니다.")
        Long photoId,

        @NotBlank(message = "imageUrl은 필수입니다.")
        String imageUrl,

        @NotBlank(message = "색상은 필수입니다.")
        String color,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        Integer price,

        @NotNull(message = "sectionId는 필수입니다.")
        Long sectionId
) {
}
