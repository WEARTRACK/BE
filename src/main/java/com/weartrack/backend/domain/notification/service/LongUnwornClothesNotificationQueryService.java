package com.weartrack.backend.domain.notification.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.dailyReview.entity.DailyReview;
import com.weartrack.backend.domain.dailyReview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LongUnwornClothesNotificationQueryService {

    private final MemberRepository memberRepository;
    private final ClothesRepository clothesRepository;
    private final DailyReviewRepository dailyReviewRepository;
    private final NotificationService notificationService;

    public List<LongUnwornClothesNotificationRequest> findNotificationRequests(
            YearMonth targetMonth
    ) {
        LocalDate periodStartDate = targetMonth.atDay(1);
        LocalDate periodEndDate = targetMonth.atDay(Math.min(28, targetMonth.lengthOfMonth()));
        List<Long> memberIds = memberRepository.findAll()
                .stream()
                .map(Member::getMemberId)
                .toList();
        Map<Long, List<String>> tokenMap = notificationService.findTokenMapEnabledFor(
                NotificationType.LONG_UNWORN_CLOTHES,
                memberIds
        );

        return memberIds.stream()
                .map(memberId -> createNotificationRequestSafely(
                        memberId,
                        targetMonth,
                        periodStartDate,
                        periodEndDate,
                        tokenMap.getOrDefault(memberId, List.of())
                ))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<LongUnwornClothesNotificationRequest> createNotificationRequestSafely(
            Long memberId,
            YearMonth targetMonth,
            LocalDate periodStartDate,
            LocalDate periodEndDate,
            List<String> tokens
    ) {
        try {
            return createNotificationRequest(
                    memberId,
                    targetMonth,
                    periodStartDate,
                    periodEndDate,
                    tokens
            );
        } catch (Exception e) {
            log.error("장기 미착용 옷 알림 대상 조회 실패: memberId={}", memberId, e);
            return Optional.empty();
        }
    }

    private Optional<LongUnwornClothesNotificationRequest> createNotificationRequest(
            Long memberId,
            YearMonth targetMonth,
            LocalDate periodStartDate,
            LocalDate periodEndDate,
            List<String> tokens
    ) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }

        Set<Long> wornClothesIds = dailyReviewRepository
                .findAllByMemberIdAndReviewDateBetween(memberId, periodStartDate, periodEndDate)
                .stream()
                .filter(DailyReview::isCompleted)
                .flatMap(review -> review.getItems().stream())
                .map(item -> item.getClothesId())
                .collect(Collectors.toSet());

        long unwornClothesCount = clothesRepository.findAllByMemberId(memberId)
                .stream()
                .filter(clothes -> wasRegisteredBeforePeriod(clothes, periodStartDate))
                .filter(clothes -> !wornClothesIds.contains(clothes.getId()))
                .count();

        if (unwornClothesCount == 0) {
            return Optional.empty();
        }

        return Optional.of(new LongUnwornClothesNotificationRequest(
                memberId,
                targetMonth,
                periodStartDate,
                periodEndDate,
                unwornClothesCount,
                tokens
        ));
    }

    private boolean wasRegisteredBeforePeriod(Clothes clothes, LocalDate periodStartDate) {
        return clothes.getCreatedAt() == null
                || !clothes.getCreatedAt().toLocalDate().isAfter(periodStartDate);
    }
}
