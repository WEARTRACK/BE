package com.weartrack.backend.domain.onboarding.entity;

import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
public class OnboardingQuest extends BaseTimeEntity {

    private static final int NEXT_QUEST_DELAY_DAYS = 7;

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

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "available_at")
    private LocalDateTime availableAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private OnboardingQuest(
            Long memberId,
            QuestType questType,
            int requiredCount,
            boolean active,
            LocalDateTime availableAt
    ) {
        this.memberId = memberId;
        this.questType = questType;
        this.requiredCount = requiredCount;
        this.currentCount = 0;
        this.completed = false;
        this.active = active;
        this.availableAt = availableAt;
        this.completedAt = null;
    }

    public static OnboardingQuest createActive(Long memberId, QuestType questType, int requiredCount) {
        return OnboardingQuest.builder()
                .memberId(memberId)
                .questType(questType)
                .requiredCount(requiredCount)
                .active(true)
                .availableAt(LocalDateTime.now())
                .build();
    }

    public static OnboardingQuest createInactive(Long memberId, QuestType questType, int requiredCount) {
        return OnboardingQuest.builder()
                .memberId(memberId)
                .questType(questType)
                .requiredCount(requiredCount)
                .active(false)
                .availableAt(null)
                .build();
    }

    public void increase(int count) {
        if (completed || !active) {
            return;
        }

        this.currentCount = Math.min(this.currentCount + count, this.requiredCount);

        if (this.currentCount >= this.requiredCount) {
            complete();
        }
    }

    public void complete() {
        this.completed = true;
        this.currentCount = this.requiredCount;
        this.completedAt = LocalDateTime.now();
    }

    public void reserveAfter(LocalDateTime baseTime) {
        this.availableAt = baseTime.plusDays(NEXT_QUEST_DELAY_DAYS);
        this.active = false;
    }

    public void activateIfAvailable(LocalDateTime now) {
        if (!completed && !active && availableAt != null && !availableAt.isAfter(now)) {
            this.active = true;
        }
    }
}