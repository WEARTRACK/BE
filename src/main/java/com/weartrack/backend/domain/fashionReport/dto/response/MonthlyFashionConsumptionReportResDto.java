package com.weartrack.backend.domain.fashionReport.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MonthlyFashionConsumptionReportResDto(
        String yearMonth,
        LocalDate monthStartDate,
        LocalDate monthEndDate,
        long totalExpenseAmount,
        Long expenseChangeRate,
        List<MonthlyExpense> monthlyExpenses,
        List<TopCategory> topCategories
) {
    public record MonthlyExpense(
            String yearMonth,
            long expenseAmount
    ) {
    }

    public record TopCategory(
            String category,
            int percentage
    ) {
    }
}
