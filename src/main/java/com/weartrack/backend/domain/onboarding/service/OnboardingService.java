package com.weartrack.backend.domain.onboarding.service;

import com.weartrack.backend.domain.onboarding.dto.response.OnboardingQuestResDto;
import com.weartrack.backend.domain.onboarding.dto.response.OnboardingQuestsResDto;
import com.weartrack.backend.domain.onboarding.dto.response.OnboardingSkipResDto;
import com.weartrack.backend.domain.onboarding.dto.response.OnboardingStatusResDto;
import com.weartrack.backend.domain.onboarding.entity.Onboarding;
import com.weartrack.backend.domain.onboarding.entity.OnboardingQuest;
import com.weartrack.backend.domain.onboarding.entity.QuestType;
import com.weartrack.backend.domain.onboarding.exception.OnboardingErrorCode;
import com.weartrack.backend.domain.onboarding.repository.OnboardingQuestRepository;
import com.weartrack.backend.domain.onboarding.repository.OnboardingRepository;
import com.weartrack.backend.global.exception.GeneralException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final OnboardingQuestRepository questRepository;

    @Transactional
    public void initializeIfNotExists(Long memberId) {
        if (onboardingRepository.existsByMemberId(memberId)) {
            return;
        }

        Onboarding onboarding = Onboarding.create(memberId);
        onboardingRepository.save(onboarding);

        questRepository.save(OnboardingQuest.create(memberId, QuestType.REGISTER_CLOSET, 1));
        questRepository.save(OnboardingQuest.create(memberId, QuestType.REGISTER_TOP, 5));
        questRepository.save(OnboardingQuest.create(memberId, QuestType.REGISTER_BOTTOM, 2));
    }

    @Transactional
    public OnboardingQuestsResDto getQuests(Long memberId) {
        initializeIfNotExists(memberId);

        Onboarding onboarding = getOnboarding(memberId);
        List<OnboardingQuest> quests = getSortedQuests(memberId);

        int completedCount = countCompletedQuests(quests);

        return new OnboardingQuestsResDto(
                onboarding.isCompleted(),
                quests.size(),
                completedCount,
                quests.stream()
                        .map(OnboardingQuestResDto::from)
                        .toList()
        );
    }

    @Transactional
    public OnboardingStatusResDto getStatus(Long memberId) {
        initializeIfNotExists(memberId);

        Onboarding onboarding = getOnboarding(memberId);
        List<OnboardingQuest> quests = questRepository.findAllByMemberId(memberId);

        return new OnboardingStatusResDto(
                onboarding.isCompleted(),
                onboarding.isHidden(),
                quests.size(),
                countCompletedQuests(quests)
        );
    }

    @Transactional
    public OnboardingSkipResDto skip(Long memberId) {
        initializeIfNotExists(memberId);

        Onboarding onboarding = getOnboarding(memberId);
        onboarding.hide();

        return new OnboardingSkipResDto(
                onboarding.isCompleted(),
                onboarding.isHidden()
        );
    }

    @Transactional
    public void completeQuest(Long memberId, QuestType questType, int incrementCount) {
        initializeIfNotExists(memberId);

        Onboarding onboarding = getOnboarding(memberId);

        if (onboarding.isCompleted()) {
            return;
        }

        OnboardingQuest quest = questRepository.findByMemberIdAndQuestType(memberId, questType)
                .orElseThrow(() -> new GeneralException(OnboardingErrorCode.QUEST_NOT_FOUND));

        quest.increase(incrementCount);

        completeOnboardingIfAllQuestsDone(memberId);
    }

    private void completeOnboardingIfAllQuestsDone(Long memberId) {
        Onboarding onboarding = getOnboarding(memberId);
        List<OnboardingQuest> quests = questRepository.findAllByMemberId(memberId);

        boolean allCompleted = quests.stream()
                .allMatch(OnboardingQuest::isCompleted);

        if (allCompleted) {
            onboarding.complete();
        }
    }

    private Onboarding getOnboarding(Long memberId) {
        return onboardingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new GeneralException(OnboardingErrorCode.ONBOARDING_NOT_FOUND));
    }

    private List<OnboardingQuest> getSortedQuests(Long memberId) {
        return questRepository.findAllByMemberId(memberId)
                .stream()
                .sorted(Comparator.comparingInt(q -> q.getQuestType().ordinal()))
                .toList();
    }

    private int countCompletedQuests(List<OnboardingQuest> quests) {
        return (int) quests.stream()
                .filter(OnboardingQuest::isCompleted)
                .count();
    }
}