package com.weartrack.backend.domain.weeklyreview.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.dailyreview.entity.DailyReview;
import com.weartrack.backend.domain.dailyreview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.weeklyreview.dto.response.WeeklyReviewSummaryResDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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

        List<DailyReview> dailyReviews = dailyReviewRepository
                .findAllByMemberIdAndReviewDateBetween(memberId, weekStartDate, weekEndDate);

        Set<Long> wornClothesIds = dailyReviews.stream()
                .filter(DailyReview::isCompleted)
                .flatMap(review -> review.getItems().stream())
                .map(item -> item.getClothesId())
                .collect(Collectors.toSet());

        List<Clothes> wornClothes = clothes.stream()
                .filter(clothesItem -> wornClothesIds.contains(clothesItem.getId()))
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

        long totalClothesCount = clothesRepository.countByMemberId(memberId);
        int usageRate = calculateUsageRate(wornClothesIds.size(), totalClothesCount);

        return new WeeklyReviewSummaryResDto(
                weekStartDate,
                weekEndDate,
                wornClothesIds.size(),
                usageRate,
                categories
        );
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
