package com.weartrack.backend.domain.onboarding.repository;

import com.weartrack.backend.domain.onboarding.entity.OnboardingQuest;
import com.weartrack.backend.domain.onboarding.entity.QuestType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingQuestRepository extends JpaRepository<OnboardingQuest, Long> {

    List<OnboardingQuest> findAllByMemberId(Long memberId);

    Optional<OnboardingQuest> findByMemberIdAndQuestType(Long memberId, QuestType questType);
}