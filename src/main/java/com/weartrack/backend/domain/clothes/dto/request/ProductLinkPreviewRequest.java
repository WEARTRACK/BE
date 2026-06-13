package com.weartrack.backend.domain.clothes.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductLinkPreviewRequest(
        @NotBlank(message = "상품 URL은 필수입니다.")
        String url
) {
}
