package com.weartrack.backend.domain.home.service;

import com.weartrack.backend.domain.closet.repository.ClosetRepository;
import com.weartrack.backend.domain.closet.repository.ClosetSectionRepository;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.dailyReview.entity.DailyReview;
import com.weartrack.backend.domain.dailyReview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.home.dto.response.HomeSummaryResDto;
import com.weartrack.backend.domain.home.dto.response.HomeWeeklyClosetUsageAnalysisResDto;
import com.weartrack.backend.domain.home.dto.response.HomeWeeklyWornClothesResDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
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
        LocalDate weekStartDate = getCurrentWeekStartDate();
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);
        long totalClothesCount = clothes.size();
        long weeklyExpenseAmount = calculateWeeklyExpenseAmount(
                clothes,
                weekStartDate,
                weekEndDate
        );
        long weeklyWornClothesCount = getWeeklyWornClothesIds(
                memberId,
                weekStartDate,
                weekEndDate
        ).size();

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

    public HomeWeeklyClosetUsageAnalysisResDto getWeeklyClosetUsageAnalysis(Long memberId) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDate weekStartDate = getCurrentWeekStartDate();
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        long totalClothesCount = clothesRepository.findAllByMemberId(memberId).size();
        long weeklyWornClothesCount = dailyReviewRepository
                .countDistinctWornClothesByMemberIdAndReviewDateBetween(
                        memberId,
                        weekStartDate,
                        weekEndDate
                );
        int weeklyClosetUsageRate = calculateWeeklyClosetUsageRate(
                totalClothesCount,
                weeklyWornClothesCount
        );

        long todayWornClothesCount = countDailyWornClothes(memberId, today);
        long unwornClothesCount = Math.max(totalClothesCount - todayWornClothesCount, 0);

        return new HomeWeeklyClosetUsageAnalysisResDto(
                weekStartDate,
                weekEndDate,
                weeklyClosetUsageRate,
                getClosetUsageType(weeklyClosetUsageRate),
                unwornClothesCount
        );
    }

    public HomeWeeklyWornClothesResDto getWeeklyWornClothes(Long memberId) {
        LocalDate weekStartDate = getCurrentWeekStartDate();
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);
        Set<Long> weeklyWornClothesIds = getWeeklyWornClothesIds(
                memberId,
                weekStartDate,
                weekEndDate
        );
        List<Clothes> weeklyWornClothes = clothes.stream()
                .filter(clothesItem -> weeklyWornClothesIds.contains(clothesItem.getId()))
                .sorted(clothesComparator())
                .toList();

        int weeklyClosetUsageRate = calculateWeeklyClosetUsageRate(
                clothes.size(),
                weeklyWornClothesIds.size()
        );

        return new HomeWeeklyWornClothesResDto(
                weeklyClosetUsageRate,
                getClosetUsageType(weeklyClosetUsageRate),
                weeklyWornClothesIds.size(),
                calculateTotalPrice(weeklyWornClothes),
                toWornClothesItems(weeklyWornClothes)
        );
    }

    private Set<Long> getWeeklyWornClothesIds(
            Long memberId,
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        return dailyReviewRepository
                .findAllByMemberIdAndReviewDateBetween(memberId, weekStartDate, weekEndDate)
                .stream()
                .filter(DailyReview::isCompleted)
                .flatMap(review -> review.getItems().stream())
                .map(item -> item.getClothesId())
                .collect(Collectors.toSet());
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

    private List<HomeWeeklyWornClothesResDto.WornClothesItem> toWornClothesItems(
            List<Clothes> clothes
    ) {
        return clothes.stream()
                .map(clothesItem -> new HomeWeeklyWornClothesResDto.WornClothesItem(
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

    private Comparator<Clothes> clothesComparator() {
        return Comparator
                .comparing((Clothes clothes) -> clothes.getCategory(), CategoryOrder.comparator())
                .thenComparing(Clothes::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private long calculateWeeklyExpenseAmount(
            List<Clothes> clothes,
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        return clothes.stream()
                .filter(clothesItem -> clothesItem.getCreatedAt() != null)
                .filter(clothesItem -> {
                    LocalDate createdDate = clothesItem.getCreatedAt().toLocalDate();
                    return !createdDate.isBefore(weekStartDate) && !createdDate.isAfter(weekEndDate);
                })
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

    private LocalDate getCurrentWeekStartDate() {
        return LocalDate.now(SEOUL_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }
}
