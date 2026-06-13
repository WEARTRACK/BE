package com.weartrack.backend.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "onboarding_quest",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_onboarding_quest_member_type",
                        columnNames = {"member_id", "quest_type"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OnboardingQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_quest_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false, length = 30)
    private QuestType questType;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @Column(name = "current_count", nullable = false)
    private int currentCount;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Builder
    private OnboardingQuest(
            Long memberId,
            QuestType questType,
            int requiredCount
    ) {
        this.memberId = memberId;
        this.questType = questType;
        this.requiredCount = requiredCount;
        this.currentCount = 0;
        this.completed = false;
    }

    public static OnboardingQuest create(Long memberId, QuestType questType, int requiredCount) {
        return OnboardingQuest.builder()
                .memberId(memberId)
                .questType(questType)
                .requiredCount(requiredCount)
                .build();
    }

    public void increase(int count) {
        if (completed) {
            return;
        }

        this.currentCount = Math.min(this.currentCount + count, this.requiredCount);

        if (this.currentCount >= this.requiredCount) {
            this.completed = true;
        }
    }
}
