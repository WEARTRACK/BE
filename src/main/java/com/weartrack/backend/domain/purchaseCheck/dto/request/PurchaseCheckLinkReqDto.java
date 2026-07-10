package com.weartrack.backend.domain.purchaseCheck.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PurchaseCheckLinkReqDto(
        @NotBlank(message = "상품 URL은 필수입니다.")
        String url
) {
}
