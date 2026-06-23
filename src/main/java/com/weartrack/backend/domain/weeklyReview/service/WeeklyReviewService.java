package com.weartrack.backend.domain.weeklyReview.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.dailyreview.entity.DailyReview;
import com.weartrack.backend.domain.dailyreview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.weeklyReview.dto.response.WeeklyReviewSummaryResDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyReviewService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final DailyReviewRepository dailyReviewRepository;
    private final ClothesRepository clothesRepository;

    public WeeklyReviewSummaryResDto getCurrentReviewSummary(Long memberId) {
        return getReviewSummary(memberId, getCurrentWeekStartDate());
    }

    public WeeklyReviewSummaryResDto getReviewSummary(Long memberId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);

        Set<Long> wornClothesIds = getWornClothesIds(memberId, weekStartDate, weekEndDate);

        List<Clothes> wornClothes = clothes.stream()
                .filter(clothesItem -> wornClothesIds.contains(clothesItem.getId()))
                .sorted(clothesComparator())
                .toList();

        List<WeeklyReviewSummaryResDto.CategoryGroup> categories = wornClothes.stream()
                .collect(Collectors.groupingBy(
                        Clothes::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> toCategoryGroup(entry.getKey(), entry.getValue()))
                .toList();

        long totalClothesCount = clothes.size();
        int usageRate = calculateUsageRate(wornClothesIds.size(), totalClothesCount);
        WeeklyReviewSummaryResDto.WeeklyInsight weeklyInsight =
                createWeeklyInsight(memberId, weekStartDate, totalClothesCount, usageRate);

        return new WeeklyReviewSummaryResDto(
                weekStartDate,
                weekEndDate,
                wornClothesIds.size(),
                usageRate,
                weeklyInsight,
                categories
        );
    }

    private Set<Long> getWornClothesIds(Long memberId, LocalDate startDate, LocalDate endDate) {
        return dailyReviewRepository
                .findAllByMemberIdAndReviewDateBetween(memberId, startDate, endDate)
                .stream()
                .filter(DailyReview::isCompleted)
                .flatMap(review -> review.getItems().stream())
                .map(item -> item.getClothesId())
                .collect(Collectors.toSet());
    }

    private WeeklyReviewSummaryResDto.CategoryGroup toCategoryGroup(
            String category,
            List<Clothes> clothes
    ) {
        List<WeeklyReviewSummaryResDto.ClothesItem> items = clothes.stream()
                .map(clothesItem -> new WeeklyReviewSummaryResDto.ClothesItem(
                        clothesItem.getId(),
                        clothesItem.getImageUrl(),
                        clothesItem.getColor()
                ))
                .toList();

        return new WeeklyReviewSummaryResDto.CategoryGroup(category, items.size(), items);
    }

    private WeeklyReviewSummaryResDto.WeeklyInsight createWeeklyInsight(
            Long memberId,
            LocalDate weekStartDate,
            long totalClothesCount,
            int usageRate
    ) {
        LocalDate previousWeekStartDate = weekStartDate.minusWeeks(1);
        LocalDate previousWeekEndDate = previousWeekStartDate.plusDays(6);
        int previousWeekUsageRate = calculateUsageRate(
                getWornClothesIds(memberId, previousWeekStartDate, previousWeekEndDate).size(),
                totalClothesCount
        );
        int usageRateChange = usageRate - previousWeekUsageRate;

        return new WeeklyReviewSummaryResDto.WeeklyInsight(
                previousWeekUsageRate,
                usageRateChange,
                createInsightMessage(usageRateChange)
        );
    }

    private String createInsightMessage(int usageRateChange) {
        if (usageRateChange > 0) {
            return "지난 주보다 옷장을 " + usageRateChange + "%를 더 활용했어요!";
        }

        if (usageRateChange < 0) {
            return "지난 주보다 옷장 활용률이 " + Math.abs(usageRateChange) + "% 낮아졌어요.";
        }

        return "지난 주와 옷장 활용률이 같아요.";
    }

    private Comparator<Clothes> clothesComparator() {
        return Comparator
                .comparing((Clothes clothes) -> clothes.getCategory(), CategoryOrder.comparator())
                .thenComparing(Clothes::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int calculateUsageRate(long wornClothesCount, long totalClothesCount) {
        if (totalClothesCount == 0) {
            return 0;
        }

        return (int) Math.round((wornClothesCount * 100.0) / totalClothesCount);
    }

    private LocalDate getCurrentWeekStartDate() {
        return LocalDate.now(SEOUL_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }
}
