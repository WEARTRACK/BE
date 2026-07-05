package com.weartrack.backend.domain.clothes.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.weartrack.backend.domain.clothes.entity.ImageStorageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClothesFromLinkRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @Size(max = 100, message = "브랜드명은 100자 이하만 입력 가능합니다.")
        String brandName,

        @NotBlank(message = "상품 원본 URL은 필수입니다.")
        @Pattern(regexp = "^https?://\\S+$", message = "상품 원본 URL은 http 또는 https 형식이어야 합니다.")
        String sourceUrl,

        @NotBlank(message = "이미지 URL은 필수입니다.")
        @Pattern(regexp = "^https?://\\S+$", message = "이미지 URL은 http 또는 https 형식이어야 합니다.")
        String imageUrl,

        @NotNull(message = "이미지 저장 타입은 필수입니다.")
        @JsonAlias("imageType")
        ImageStorageType imageStorageType,

        @NotBlank(message = "색상은 필수입니다.")
        String color,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        Integer price,

        LocalDate purchaseDate,

        String storageLocation,

        @NotNull(message = "옷장 sectionId는 필수입니다.")
        Long sectionId
) {
}