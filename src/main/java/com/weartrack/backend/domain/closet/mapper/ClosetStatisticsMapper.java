package com.weartrack.backend.domain.closet.mapper;

import com.weartrack.backend.domain.closet.dto.response.ClosetStatisticsDto;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClosetStatisticsMapper {
    private static final int TOP_N = 4;
    private static final String OTHERS = "Others";

    public ClosetStatisticsDto toStatistics(List<Clothes> clothesList) {
        int totalCount = clothesList.size();
        List<ClosetStatisticsDto.CategoryStatistics> sorted = sortByCountDesc(clothesList);
        List<ClosetStatisticsDto.CategoryStatistics> result = mergeIntoTopAndOthers(sorted);

        return new ClosetStatisticsDto(totalCount, result);
    }

    private List<ClosetStatisticsDto.CategoryStatistics> sortByCountDesc(List<Clothes> clothesList) {
        return clothesList.stream()
                .collect(Collectors.groupingBy(
                        Clothes::getCategory,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new ClosetStatisticsDto.CategoryStatistics(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator
                        .comparing(ClosetStatisticsDto.CategoryStatistics::count).reversed()
                        .thenComparing(ClosetStatisticsDto.CategoryStatistics::category))
                .toList();
    }

    private List<ClosetStatisticsDto.CategoryStatistics> mergeIntoTopAndOthers(List<ClosetStatisticsDto.CategoryStatistics> sorted) {
        if (sorted.size() <= TOP_N) {
            return sorted;
        }

        List<ClosetStatisticsDto.CategoryStatistics> result = new ArrayList<>(sorted.subList(0, TOP_N));

        int othersCount = sorted.subList(TOP_N, sorted.size()).stream()
                .mapToInt(ClosetStatisticsDto.CategoryStatistics::count)
                .sum();

        result.add(new ClosetStatisticsDto.CategoryStatistics(OTHERS, othersCount));
        return result;
    }
}
