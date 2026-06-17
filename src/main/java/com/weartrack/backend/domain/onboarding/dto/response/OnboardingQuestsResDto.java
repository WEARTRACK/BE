package com.weartrack.backend.domain.onboarding.dto.response;

import java.util.List;

public record OnboardingQuestsResDto(
        boolean onboardingCompleted,
        int totalQuestCount,
        int completedQuestCount,
        List<OnboardingQuestResDto> quests
) {
}