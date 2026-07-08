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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final OnboardingQuestRepository questRepository;

    @Transactional
    public void initializeIfNotExists(Long memberId) {
        if (onboardingRepository.findByMemberId(memberId).isEmpty()) {
            try {
                onboardingRepository.saveAndFlush(Onboarding.create(memberId));
            } catch (DataIntegrityViolationException e) {
                // 동시에 다른 요청이 먼저 생성한 경우이므로 무시
            }
        }

        initializeQuestIfNotExists(memberId, QuestType.REGISTER_CLOSET, 1, true);
        initializeQuestIfNotExists(memberId, QuestType.REGISTER_TOP, 5, false);
        initializeQuestIfNotExists(memberId, QuestType.REGISTER_BOTTOM, 2, false);
    }

    private void initializeQuestIfNotExists(
            Long memberId,
            QuestType questType,
            int requiredCount,
            boolean active
    ) {
        if (questRepository.existsByMemberIdAndQuestType(memberId, questType)) {
            return;
        }

        try {
            OnboardingQuest quest = active
                    ? OnboardingQuest.createActive(memberId, questType, requiredCount)
                    : OnboardingQuest.createInactive(memberId, questType, requiredCount);

            questRepository.saveAndFlush(quest);
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 요청이 같은 퀘스트를 먼저 생성한 경우이므로 무시
        }
    }

    @Transactional
    public OnboardingQuestsResDto getQuests(Long memberId) {
        initializeIfNotExists(memberId);
        activateAvailableQuests(memberId);

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
        activateAvailableQuests(memberId);

        Onboarding onboarding = getOnboarding(memberId);
        List<OnboardingQuest> quests = questRepository.findAllByMemberId(memberId);

        LocalDateTime nextQuestOpenAt = quests.stream()
                .filter(quest -> !quest.isCompleted())
                .filter(quest -> !quest.isActive())
                .map(OnboardingQuest::getAvailableAt)
                .filter(availableAt -> availableAt != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        int availableQuestCount = (int) quests.stream()
                .filter(quest -> !quest.isCompleted())
                .filter(OnboardingQuest::isActive)
                .count();

        boolean hasNewQuest = availableQuestCount > 0;

        return new OnboardingStatusResDto(
                onboarding.isCompleted(),
                onboarding.isHidden(),
                quests.size(),
                countCompletedQuests(quests),
                hasNewQuest,
                availableQuestCount,
                nextQuestOpenAt
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
        activateAvailableQuests(memberId);

        Onboarding onboarding = getOnboarding(memberId);

        if (onboarding.isCompleted()) {
            return;
        }

        OnboardingQuest quest = questRepository.findByMemberIdAndQuestType(memberId, questType)
                .orElseThrow(() -> new GeneralException(OnboardingErrorCode.QUEST_NOT_FOUND));

        boolean wasCompleted = quest.isCompleted();

        quest.increase(incrementCount);

        if (!wasCompleted && quest.isCompleted()) {
            reserveNextQuest(memberId, quest.getQuestType(), quest.getCompletedAt());
        }

        completeOnboardingIfAllQuestsDone(memberId);
    }

    private void reserveNextQuest(Long memberId, QuestType completedQuestType, LocalDateTime completedAt) {
        QuestType nextQuestType = getNextQuestType(completedQuestType);

        if (nextQuestType == null) {
            return;
        }

        OnboardingQuest nextQuest = questRepository.findByMemberIdAndQuestType(memberId, nextQuestType)
                .orElseThrow(() -> new GeneralException(OnboardingErrorCode.QUEST_NOT_FOUND));

        if (!nextQuest.isCompleted()) {
            nextQuest.reserveAfter(completedAt);
        }
    }

    private QuestType getNextQuestType(QuestType questType) {
        return switch (questType) {
            case REGISTER_CLOSET -> QuestType.REGISTER_TOP;
            case REGISTER_TOP -> QuestType.REGISTER_BOTTOM;
            case REGISTER_BOTTOM -> null;
        };
    }

    private void activateAvailableQuests(Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        questRepository.findAllByMemberId(memberId)
                .forEach(quest -> quest.activateIfAvailable(now));
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