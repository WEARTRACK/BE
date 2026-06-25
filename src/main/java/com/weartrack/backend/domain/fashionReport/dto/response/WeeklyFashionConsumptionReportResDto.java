package com.weartrack.backend.domain.fashionReport.dto.response;

import java.time.LocalDate;
import java.util.List;

public record WeeklyFashionConsumptionReportResDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        long totalExpenseAmount,
        Long expenseChangeRate,
        List<CategoryExpense> categories
) {
    public record CategoryExpense(
            String category,
            long expenseAmount
    ) {
    }
}
