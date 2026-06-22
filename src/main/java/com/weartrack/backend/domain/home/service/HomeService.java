package com.weartrack.backend.domain.home.service;

import com.weartrack.backend.domain.closet.repository.ClosetRepository;
import com.weartrack.backend.domain.closet.repository.ClosetSectionRepository;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.dailyreview.entity.DailyReview;
import com.weartrack.backend.domain.dailyreview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.home.dto.HomeSummaryResDto;
import com.weartrack.backend.domain.home.dto.HomeWeeklyClosetUsageResDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final String MASTER_TYPE = "MASTER";
    private static final String ACTIVE_TYPE = "ACTIVE";
    private static final String POTENTIAL_TYPE = "POTENTIAL";
    private static final String NEGLECTED_TYPE = "NEGLECTED";

    private final ClothesRepository clothesRepository;
    private final DailyReviewRepository dailyReviewRepository;
    private final ClosetRepository closetRepository;
    private final ClosetSectionRepository closetSectionRepository;

    public HomeSummaryResDto getHomeSummary(Long memberId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        LocalDate weekStartDate = LocalDate.now(SEOUL_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        long totalClothesCount = clothesRepository.countByMemberId(memberId);
        long weeklyExpenseAmount = clothesRepository.sumWeeklyExpenseAmount(memberId, oneWeekAgo);
        long weeklyWornClothesCount = dailyReviewRepository
                .countDistinctWornClothesByMemberIdAndReviewDateBetween(
                        memberId,
                        weekStartDate,
                        weekEndDate
                );

        long closetCount = closetRepository.countByMemberId(memberId);
        long storageCount = closetSectionRepository.countByMemberId(memberId);

        int weeklyClosetUsageRate = calculateWeeklyClosetUsageRate(
                totalClothesCount,
                weeklyWornClothesCount
        );

        return new HomeSummaryResDto(
                totalClothesCount,
                weeklyExpenseAmount,
                weeklyClosetUsageRate,
                closetCount,
                storageCount
        );
    }

    public HomeWeeklyClosetUsageResDto getWeeklyClosetUsage(Long memberId) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);
        List<DailyReview> weeklyReviews = dailyReviewRepository
                .findAllByMemberIdAndReviewDateBetween(memberId, weekStartDate, weekEndDate);

        Set<Long> weeklyWornClothesIds = weeklyReviews.stream()
                .filter(DailyReview::isCompleted)
                .flatMap(review -> review.getItems().stream())
                .map(item -> item.getClothesId())
                .collect(Collectors.toSet());

        List<Clothes> weeklyWornClothes = clothes.stream()
                .filter(clothesItem -> weeklyWornClothesIds.contains(clothesItem.getId()))
                .toList();

        long totalClothesCount = clothes.size();
        int weeklyClosetUsageRate = calculateWeeklyClosetUsageRate(
                totalClothesCount,
                weeklyWornClothesIds.size()
        );

        long todayWornClothesCount = countDailyWornClothes(memberId, today);
        long unwornClothesCount = Math.max(totalClothesCount - todayWornClothesCount, 0);

        return new HomeWeeklyClosetUsageResDto(
                weekStartDate,
                weekEndDate,
                today,
                weeklyClosetUsageRate,
                getClosetUsageType(weeklyClosetUsageRate),
                getClosetUsageLabel(weeklyClosetUsageRate),
                getClosetUsageRange(weeklyClosetUsageRate),
                unwornClothesCount,
                createUsageMessage(unwornClothesCount),
                weeklyWornClothesIds.size(),
                calculateTotalPrice(weeklyWornClothes),
                toWornClothesItems(weeklyWornClothes)
        );
    }

    private int calculateWeeklyClosetUsageRate(
            long totalClothesCount,
            long weeklyWornClothesCount
    ) {
        if (totalClothesCount == 0) {
            return 0;
        }

        return (int) Math.round((weeklyWornClothesCount * 100.0) / totalClothesCount);
    }

    private long countDailyWornClothes(Long memberId, LocalDate reviewDate) {
        return dailyReviewRepository.findByMemberIdAndReviewDate(memberId, reviewDate)
                .filter(DailyReview::isCompleted)
                .map(review -> review.getItems().stream()
                        .map(item -> item.getClothesId())
                        .distinct()
                        .count())
                .orElse(0L);
    }

    private List<HomeWeeklyClosetUsageResDto.WornClothesItem> toWornClothesItems(
            List<Clothes> clothes
    ) {
        return clothes.stream()
                .map(clothesItem -> new HomeWeeklyClosetUsageResDto.WornClothesItem(
                        clothesItem.getId(),
                        clothesItem.getImageUrl(),
                        clothesItem.getPrice()
                ))
                .toList();
    }

    private long calculateTotalPrice(List<Clothes> clothes) {
        return clothes.stream()
                .map(Clothes::getPrice)
                .filter(price -> price != null)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private String getClosetUsageType(int usageRate) {
        if (usageRate >= 81) {
            return MASTER_TYPE;
        }

        if (usageRate >= 51) {
            return ACTIVE_TYPE;
        }

        if (usageRate >= 21) {
            return POTENTIAL_TYPE;
        }

        return NEGLECTED_TYPE;
    }

    private String getClosetUsageLabel(int usageRate) {
        return switch (getClosetUsageType(usageRate)) {
            case MASTER_TYPE -> "마스터형 옷장";
            case ACTIVE_TYPE -> "활용형 옷장";
            case POTENTIAL_TYPE -> "잠재형 옷장";
            default -> "방치형 옷장";
        };
    }

    private String getClosetUsageRange(int usageRate) {
        return switch (getClosetUsageType(usageRate)) {
            case MASTER_TYPE -> "81-100% 활용";
            case ACTIVE_TYPE -> "51-80% 활용";
            case POTENTIAL_TYPE -> "21-50% 활용";
            default -> "0-20% 활용";
        };
    }

    private String createUsageMessage(long unwornClothesCount) {
        return "입지 않은 옷이 " + unwornClothesCount + "벌 있어요!";
    }
}
