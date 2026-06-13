package com.weartrack.backend.domain.onboarding.dto.response;

public record OnboardingSkipResDto(
        boolean onboardingCompleted,
        boolean hidden
) {
}