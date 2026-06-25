package com.weartrack.backend.domain.fashionReport.service;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.fashionReport.dto.response.MonthlyFashionConsumptionReportResDto;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyFashionConsumptionReportService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final ClothesRepository clothesRepository;

    public MonthlyFashionConsumptionReportResDto getCurrentMonthlyReport(Long memberId) {
        return getMonthlyReport(memberId, YearMonth.now(SEOUL_ZONE));
    }

    public MonthlyFashionConsumptionReportResDto getMonthlyReport(
            Long memberId,
            YearMonth yearMonth
    ) {
        LocalDate monthStartDate = yearMonth.atDay(1);
        LocalDate monthEndDate = yearMonth.atEndOfMonth();
        List<Clothes> clothes = clothesRepository.findAllByMemberId(memberId);
        List<Clothes> targetMonthClothes = filterClothesRegisteredBetween(
                clothes,
                monthStartDate,
                monthEndDate
        );
        long totalExpenseAmount = calculateTotalPrice(targetMonthClothes);

        YearMonth previousMonth = yearMonth.minusMonths(1);
        long previousMonthExpenseAmount = calculateTotalPrice(filterClothesRegisteredBetween(
                clothes,
                previousMonth.atDay(1),
                previousMonth.atEndOfMonth()
        ));

        return new MonthlyFashionConsumptionReportResDto(
                yearMonth.toString(),
                monthStartDate,
                monthEndDate,
                totalExpenseAmount,
                calculateExpenseChangeRate(totalExpenseAmount, previousMonthExpenseAmount),
                createMonthlyExpenses(clothes, getCurrentMonth()),
                createTopCategories(targetMonthClothes, totalExpenseAmount)
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
                    // TODO: 저장 시각 정책 정리 후 notification.time-zone 기준 날짜로 변환해 월을 분류한다.
                    LocalDate createdDate = clothesItem.getCreatedAt().toLocalDate();
                    return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
                })
                .toList();
    }

    private List<MonthlyFashionConsumptionReportResDto.MonthlyExpense> createMonthlyExpenses(
            List<Clothes> clothes,
            YearMonth targetMonth
    ) {
        return IntStream.rangeClosed(0, 3)
                .mapToObj(monthOffset -> targetMonth.minusMonths(3L - monthOffset))
                .map(yearMonth -> new MonthlyFashionConsumptionReportResDto.MonthlyExpense(
                        yearMonth.toString(),
                        calculateTotalPrice(filterClothesRegisteredBetween(
                                clothes,
                                yearMonth.atDay(1),
                                yearMonth.atEndOfMonth()
                        ))
                ))
                .toList();
    }

    private List<MonthlyFashionConsumptionReportResDto.TopCategory> createTopCategories(
            List<Clothes> clothes,
            long totalExpenseAmount
    ) {
        if (totalExpenseAmount == 0) {
            return List.of();
        }

        Map<String, Long> categoryExpenseMap = clothes.stream()
                .collect(Collectors.groupingBy(
                        clothesItem -> CategoryOrder.normalize(clothesItem.getCategory()),
                        Collectors.summingLong(clothesItem -> clothesItem.getPrice() == null
                                ? 0L
                                : clothesItem.getPrice())
                ));

        return categoryExpenseMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Comparator
                        .<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(entry -> CategoryOrder.orderIndex(entry.getKey()))
                        .thenComparing(Map.Entry::getKey))
                .limit(3)
                .map(entry -> new MonthlyFashionConsumptionReportResDto.TopCategory(
                        entry.getKey(),
                        calculatePercentage(entry.getValue(), totalExpenseAmount)
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

    private int calculatePercentage(long categoryExpenseAmount, long totalExpenseAmount) {
        if (totalExpenseAmount == 0) {
            return 0;
        }

        return (int) Math.round((categoryExpenseAmount * 100.0) / totalExpenseAmount);
    }

    private Long calculateExpenseChangeRate(
            long totalExpenseAmount,
            long previousMonthExpenseAmount
    ) {
        if (previousMonthExpenseAmount == 0) {
            return totalExpenseAmount == 0 ? 0L : null;
        }

        return Math.round(
                ((totalExpenseAmount - previousMonthExpenseAmount) * 100.0)
                        / previousMonthExpenseAmount
        );
    }

    private YearMonth getCurrentMonth() {
        return YearMonth.now(SEOUL_ZONE);
    }
}
