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
    }
}