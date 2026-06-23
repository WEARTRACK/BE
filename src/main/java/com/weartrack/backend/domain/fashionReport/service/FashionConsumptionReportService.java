package com.weartrack.backend.domain.fashionReport.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.fashionReport.dto.response.WeeklyFashionConsumptionReportResDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FashionConsumptionReportService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final ClothesRepository clothesRepository;

    public WeeklyFashionConsumptionReportResDto getCurrentWeeklyReport(Long memberId) {
        return getWeeklyReport(memberId, getCurrentWeekStartDate());
    }

    public WeeklyFashionConsumptionReportResDto getWeeklyReport(
            Long memberId,
            LocalDate weekStartDate
    ) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);

        List<Clothes> targetWeekClothes = filterClothesRegisteredBetween(
                clothes,
                weekStartDate,
                weekEndDate
        );
        long totalExpenseAmount = calculateTotalPrice(targetWeekClothes);

        LocalDate previousWeekStartDate = weekStartDate.minusWeeks(1);
        LocalDate previousWeekEndDate = previousWeekStartDate.plusDays(6);
        long previousWeekExpenseAmount = calculateTotalPrice(
                filterClothesRegisteredBetween(clothes, previousWeekStartDate, previousWeekEndDate)
        );

        return new WeeklyFashionConsumptionReportResDto(
                weekStartDate,
                weekEndDate,
                totalExpenseAmount,
                calculateExpenseChangeRate(totalExpenseAmount, previousWeekExpenseAmount),
                createCategoryExpenses(targetWeekClothes, totalExpenseAmount)
        );
    }

    private List<Clothes> filterClothesRegisteredBetween(
            List<Clothes> clothes,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return clothes.stream()
                .filter(clothesItem -> clothesItem.getCreatedAt() != null)
                .filter(clothesItem -> {
                    LocalDate createdDate = clothesItem.getCreatedAt().toLocalDate();
                    return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
                })
                .toList();
    }

    private List<WeeklyFashionConsumptionReportResDto.CategoryExpense> createCategoryExpenses(
            List<Clothes> clothes,
            long totalExpenseAmount
    ) {
        Map<String, Long> categoryExpenseMap = clothes.stream()
                .collect(Collectors.groupingBy(
                        clothesItem -> CategoryOrder.normalize(clothesItem.getCategory()),
                        Collectors.summingLong(clothesItem -> clothesItem.getPrice() == null
                                ? 0L
                                : clothesItem.getPrice())
                ));

        Map<String, Long> orderedCategoryExpenseMap = new LinkedHashMap<>();
        CategoryOrder.orderedCategories()
                .forEach(category -> orderedCategoryExpenseMap.put(
                        category,
                        categoryExpenseMap.getOrDefault(category, 0L)
                ));

        categoryExpenseMap.entrySet().stream()
                .filter(entry -> !orderedCategoryExpenseMap.containsKey(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> orderedCategoryExpenseMap.put(entry.getKey(), entry.getValue()));

        return orderedCategoryExpenseMap.entrySet().stream()
                .map(entry -> new WeeklyFashionConsumptionReportResDto.CategoryExpense(
                        entry.getKey(),
                        entry.getValue()
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

    private Long calculateExpenseChangeRate(
            long totalExpenseAmount,
            long previousWeekExpenseAmount
    ) {
        if (previousWeekExpenseAmount == 0) {
            return totalExpenseAmount == 0 ? 0L : null;
        }

        return Math.round(
                ((totalExpenseAmount - previousWeekExpenseAmount) * 100.0)
                        / previousWeekExpenseAmount
        );
    }

    private LocalDate getCurrentWeekStartDate() {
        return LocalDate.now(SEOUL_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }
}
