package com.weartrack.backend.domain.notification.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.dailyReview.entity.DailyReview;
import com.weartrack.backend.domain.dailyReview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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
public class LongUnwornClothesNotificationService {

    private static final String TITLE = "WEARTRACK";
    private static final String BODY_FORMAT = "오랫동안 안 입은 옷이 %d벌 있어요! 클릭해서 확인해보세요.";

    private final MemberRepository memberRepository;
    private final ClothesRepository clothesRepository;
    private final DailyReviewRepository dailyReviewRepository;
    private final NotificationService notificationService;
    private final FcmMessageSender fcmMessageSender;

    public void sendMonthlyLongUnwornClothesNotifications(YearMonth targetMonth) {
        LocalDate periodStartDate = targetMonth.atDay(1);
        LocalDate periodEndDate = targetMonth.atDay(Math.min(28, targetMonth.lengthOfMonth()));

        memberRepository.findAll()
                .stream()
                .map(Member::getMemberId)
                .forEach(memberId -> sendIfLongUnwornClothesExist(
                        memberId,
                        targetMonth,
                        periodStartDate,
                        periodEndDate
                ));
    }

    private void sendIfLongUnwornClothesExist(
            Long memberId,
            YearMonth targetMonth,
            LocalDate periodStartDate,
            LocalDate periodEndDate
    ) {
        List<String> tokens = notificationService.findTokensEnabledFor(
                NotificationType.LONG_UNWORN_CLOTHES,
                List.of(memberId)
        );
        if (tokens.isEmpty()) {
            return;
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
            return;
        }

        Map<String, String> data = Map.of(
                "type", "LONG_UNWORN_CLOTHES",
                "screen", "LONG_UNWORN_CLOTHES",
                "targetMonth", targetMonth.toString(),
                "periodStartDate", periodStartDate.toString(),
                "periodEndDate", periodEndDate.toString(),
                "unwornClothesCount", String.valueOf(unwornClothesCount)
        );

        fcmMessageSender.sendToTokens(
                tokens,
                TITLE,
                BODY_FORMAT.formatted(unwornClothesCount),
                data
        );
        log.info(
                "장기 미착용 옷 알림 발송 요청: memberId={}, targetMonth={}, unwornClothesCount={}",
                memberId,
                targetMonth,
                unwornClothesCount
        );
    }

    private boolean wasRegisteredBeforePeriod(Clothes clothes, LocalDate periodStartDate) {
        return clothes.getCreatedAt() == null
                || !clothes.getCreatedAt().toLocalDate().isAfter(periodStartDate);
    }
}
