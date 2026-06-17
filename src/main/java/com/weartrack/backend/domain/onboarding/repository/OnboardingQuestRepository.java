package com.weartrack.backend.domain.onboarding.repository;

import com.weartrack.backend.domain.onboarding.entity.OnboardingQuest;
import com.weartrack.backend.domain.onboarding.entity.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingQuestRepository extends JpaRepository<OnboardingQuest, Long> {

    List<OnboardingQuest> findAllByMemberId(Long memberId);

    Optional<OnboardingQuest> findByMemberIdAndQuestType(Long memberId, QuestType questType);

    boolean existsByMemberIdAndQuestType(Long memberId, QuestType questType);
}