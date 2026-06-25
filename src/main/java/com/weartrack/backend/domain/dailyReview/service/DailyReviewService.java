package com.weartrack.backend.domain.dailyReview.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.dailyReview.dto.request.DailyReviewSaveReqDto;
import com.weartrack.backend.domain.dailyReview.dto.response.DailyReviewEntryResDto;
import com.weartrack.backend.domain.dailyReview.entity.DailyReview;
import com.weartrack.backend.domain.dailyReview.exception.DailyReviewErrorCode;
import com.weartrack.backend.domain.dailyReview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.weeklyReview.dto.response.WeeklyReviewSummaryResDto;
import com.weartrack.backend.domain.weeklyReview.service.WeeklyReviewService;
import com.weartrack.backend.global.exception.GeneralException;
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
public class DailyReviewService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final DailyReviewRepository dailyReviewRepository;
    private final ClothesRepository clothesRepository;
    private final WeeklyReviewService weeklyReviewService;

    public DailyReviewEntryResDto getTodayReview(Long memberId) {
        return getReview(memberId, LocalDate.now(SEOUL_ZONE));
    }

    private DailyReviewEntryResDto getReview(Long memberId, LocalDate reviewDate) {
        LocalDate weekStartDate = getWeekStartDate(reviewDate);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);

        DailyReview review = dailyReviewRepository
                .findByMemberIdAndReviewDate(memberId, reviewDate)
                .orElse(null);

        Set<Long> selectedIds = review == null
                ? Set.of()
                : review.getItems().stream()
                .map(item -> item.getClothesId())
                .collect(Collectors.toSet());

        List<DailyReviewEntryResDto.CategoryGroup> categories = clothes.stream()
                .sorted(clothesComparator())
                .collect(Collectors.groupingBy(
                        Clothes::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> toCategoryGroup(entry.getKey(), entry.getValue(), selectedIds))
                .toList();

        return new DailyReviewEntryResDto(
                reviewDate,
                weekStartDate,
                weekEndDate,
                review != null && review.isCompleted(),
                isPreviousDayIncomplete(memberId, reviewDate),
                clothes.isEmpty(),
                categories
        );
    }

    @Transactional
    public WeeklyReviewSummaryResDto saveTodayReview(
            Long memberId,
            DailyReviewSaveReqDto request
    ) {
        LocalDate reviewDate = LocalDate.now(SEOUL_ZONE);
        List<Long> clothesIds = request.clothesIds().stream()
                .distinct()
                .toList();

        validateOwnedClothes(memberId, clothesIds);

        DailyReview review = dailyReviewRepository
                .findByMemberIdAndReviewDate(memberId, reviewDate)
                .map(existingReview -> {
                    if (existingReview.isCompleted()) {
                        throw new GeneralException(
                                DailyReviewErrorCode.DAILY_REVIEW_ALREADY_EXISTS
                        );
                    }

                    return existingReview;
                })
                .orElseGet(() -> DailyReview.builder()
                        .memberId(memberId)
                        .reviewDate(reviewDate)
                        .build());

        review.complete(clothesIds);
        dailyReviewRepository.save(review);

        LocalDate weekStartDate = getWeekStartDate(reviewDate);
        return weeklyReviewService.getReviewSummary(
                memberId,
                weekStartDate
        );
    }

    private DailyReviewEntryResDto.CategoryGroup toCategoryGroup(
            String category,
            List<Clothes> clothes,
            Set<Long> selectedIds
    ) {
        List<DailyReviewEntryResDto.ClothesItem> items = clothes.stream()
                .map(clothesItem -> new DailyReviewEntryResDto.ClothesItem(
                        clothesItem.getId(),
                        clothesItem.getImageUrl(),
                        clothesItem.getColor(),
                        clothesItem.getCategory(),
                        selectedIds.contains(clothesItem.getId())
                ))
                .toList();

        int selectedCount = (int) items.stream()
                .filter(DailyReviewEntryResDto.ClothesItem::selected)
                .count();

        return new DailyReviewEntryResDto.CategoryGroup(category, selectedCount, items);
    }

    private Comparator<Clothes> clothesComparator() {
        return Comparator
                .comparing((Clothes clothes) -> clothes.getCategory(), CategoryOrder.comparator())
                .thenComparing(Clothes::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private void validateOwnedClothes(Long memberId, List<Long> clothesIds) {
        if (clothesIds.isEmpty()) {
            return;
        }

        long ownedCount = clothesRepository.countOwnedClothesByIds(memberId, clothesIds);

        if (ownedCount != clothesIds.size()) {
            throw new GeneralException(DailyReviewErrorCode.INVALID_CLOTHES_SELECTION);
        }
    }

    private boolean isPreviousDayIncomplete(Long memberId, LocalDate reviewDate) {
        LocalDate previousDate = reviewDate.minusDays(1);

        return dailyReviewRepository.findByMemberIdAndReviewDate(memberId, previousDate)
                .map(review -> !review.isCompleted())
                .orElseGet(() -> dailyReviewRepository.existsByMemberIdAndReviewDateBefore(
                        memberId,
                        previousDate
                ));
    }

    private LocalDate getWeekStartDate(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }
}
