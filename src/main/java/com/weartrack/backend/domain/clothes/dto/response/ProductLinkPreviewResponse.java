package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.clothes.entity.SourceShop;

public record ProductLinkPreviewResponse(
        SourceShop sourceShop,
        String sourceUrl,
        String productName,
        String imageUrl,
        Integer price,
        String brandName,
        String category,
        String color,
        String reasonCode
) {
}
