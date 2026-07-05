package com.weartrack.backend.domain.clothes.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ClothesUpdateRequest(

        @Size(max = 100, message = "상품명은 100자 이하만 입력 가능합니다.")
        String productName,

        @Size(max = 100, message = "브랜드명은 100자 이하만 입력 가능합니다.")
        String brandName,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        Long closetId,

        Long sectionId
) {
}