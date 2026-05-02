package com.weartrack.backend.domain.closet.dto.response;

import com.weartrack.backend.domain.clothes.entity.Clothes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ClosetStatisticsDto(
        Integer totalCount,
        List<CategoryStatistics> categoryStatistics
) {
    public record CategoryStatistics(
            String category,
            Integer count
    ) {
        public static CategoryStatistics of(String category, int count) {

            return new CategoryStatistics(category, count);
        }
    }

    public static ClosetStatisticsDto from(List<Clothes> clothesList) {
        int totalCount = clothesList.size();

        Map<String, Long> countByCategory = clothesList.stream()
                .collect(Collectors.groupingBy(
                        Clothes::getCategory,
                        Collectors.counting()
                ));

        List<CategoryStatistics> stats = countByCategory.entrySet().stream()
                .map(e -> CategoryStatistics.of(e.getKey(), e.getValue().intValue()))
                .sorted((a, b) -> b.count().compareTo(a.count()))
                .toList();

        return new ClosetStatisticsDto(totalCount, stats);
    }
}