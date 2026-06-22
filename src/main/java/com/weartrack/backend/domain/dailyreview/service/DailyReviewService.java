package com.weartrack.backend.domain.dailyreview.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.dailyreview.dto.request.DailyReviewSaveReqDto;
import com.weartrack.backend.domain.dailyreview.dto.response.DailyReviewEntryResDto;
import com.weartrack.backend.domain.dailyreview.dto.response.DailyReviewSaveResDto;
import com.weartrack.backend.domain.dailyreview.entity.DailyReview;
import com.weartrack.backend.domain.dailyreview.exception.DailyReviewErrorCode;
import com.weartrack.backend.domain.dailyreview.repository.DailyReviewRepository;
import com.weartrack.backend.domain.weeklyreview.dto.response.WeeklyReviewSummaryResDto;
import com.weartrack.backend.domain.weeklyreview.service.WeeklyReviewService;
import com.weartrack.backend.global.exception.GeneralException;
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
public class DailyReviewService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final DailyReviewRepository dailyReviewRepository;
    private final ClothesRepository clothesRepository;
    private final WeeklyReviewService weeklyReviewService;

    public DailyReviewEntryResDto getTodayReview(Long memberId) {
        return getReview(memberId, LocalDate.now(SEOUL_ZONE));
    }

    public DailyReviewEntryResDto getReview(Long memberId, LocalDate reviewDate) {
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
    public DailyReviewSaveResDto saveReview(
            Long memberId,
            LocalDate reviewDate,
            DailyReviewSaveReqDto request
    ) {
        List<Long> clothesIds = request.clothesIds().stream()
                .distinct()
                .toList();

        validateOwnedClothes(memberId, clothesIds);

        dailyReviewRepository.findByMemberIdAndReviewDate(memberId, reviewDate)
                .ifPresent(review -> {
                    throw new GeneralException(DailyReviewErrorCode.DAILY_REVIEW_ALREADY_EXISTS);
                });

        DailyReview review = DailyReview.builder()
                .memberId(memberId)
                .reviewDate(reviewDate)
                .build();

        review.replaceItems(clothesIds);
        DailyReview savedReview = dailyReviewRepository.save(review);

        LocalDate weekStartDate = getWeekStartDate(reviewDate);
        WeeklyReviewSummaryResDto weeklyReview = weeklyReviewService.getReviewSummary(
                memberId,
                weekStartDate
        );

        return new DailyReviewSaveResDto(
                savedReview.getReviewDate(),
                clothesIds.size(),
                clothesIds.isEmpty(),
                weeklyReview
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
