package com.weartrack.backend.domain.onboarding.dto.response;

import java.time.LocalDateTime;

public record OnboardingStatusResDto(
        boolean onboardingCompleted,
        boolean hidden,
        int totalQuestCount,
        int completedQuestCount,

        boolean hasNewQuest,
        int availableQuestCount,
        LocalDateTime nextQuestOpenAt
) {
}