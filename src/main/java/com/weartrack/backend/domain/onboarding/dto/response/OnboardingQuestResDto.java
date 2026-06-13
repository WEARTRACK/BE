package com.weartrack.backend.domain.onboarding.dto.response;

import com.weartrack.backend.domain.onboarding.entity.OnboardingQuest;
import com.weartrack.backend.domain.onboarding.entity.QuestType;

public record OnboardingQuestResDto(
        QuestType questType,
        String title,
        String description,
        int requiredCount,
        int currentCount,
        boolean completed
) {
    public static OnboardingQuestResDto from(OnboardingQuest quest) {
        return new OnboardingQuestResDto(
                quest.getQuestType(),
                getTitle(quest.getQuestType()),
                getDescription(quest.getQuestType()),
                quest.getRequiredCount(),
                quest.getCurrentCount(),
                quest.isCompleted()
        );
    }

    private static String getTitle(QuestType questType) {
        return switch (questType) {
            case REGISTER_CLOSET -> "옷장 등록하기";
            case REGISTER_TOP -> "상의 5벌 등록하기";
            case REGISTER_BOTTOM -> "하의 2벌 등록하기";
        };
    }

    private static String getDescription(QuestType questType) {
        return switch (questType) {
            case REGISTER_CLOSET -> "나의 옷장을 등록해보세요.";
            case REGISTER_TOP -> "상의 카테고리 옷을 5벌 등록해보세요.";
            case REGISTER_BOTTOM -> "하의 카테고리 옷을 2벌 등록해보세요.";
        };
    }
}