package com.weartrack.backend.domain.onboarding.dto.response;

public record OnboardingStatusResDto(
        boolean onboardingCompleted,
        boolean hidden,
        int totalQuestCount,
        int completedQuestCount
) {
}