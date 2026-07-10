package com.weartrack.backend.domain.clothes.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClothesCreateRequest(

        @NotNull(message = "옷 photoId는 필수입니다.")
        Long photoId,

        @Size(max = 100, message = "상품명은 100자 이하만 입력 가능합니다.")
        String productName,

        @Size(max = 100, message = "브랜드명은 100자 이하만 입력 가능합니다.")
        String brandName,

        @NotBlank(message = "색상은 필수입니다.")
        String color,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        LocalDate purchaseDate,

        @NotNull(message = "옷장 closetId는 필수입니다.")
        Long closetId,

        @NotNull(message = "옷장 sectionId는 필수입니다.")
        Long sectionId
) {
}