package com.weartrack.backend.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record RequiredTermsAgreementReqDto(
        @NotNull
        Boolean requiredTermsAgreed
) {
}
