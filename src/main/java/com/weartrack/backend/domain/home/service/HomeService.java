package com.weartrack.backend.domain.home.service;

import com.weartrack.backend.domain.closet.repository.ClosetRepository;
import com.weartrack.backend.domain.closet.repository.ClosetSectionRepository;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.home.dto.HomeSummaryResDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final int TEMP_WEEKLY_CLOSET_USAGE_RATE = 73;

    private final ClothesRepository clothesRepository;
    private final ClosetRepository closetRepository;
    private final ClosetSectionRepository closetSectionRepository;

    public HomeSummaryResDto getHomeSummary(Long memberId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        long totalClothesCount = clothesRepository.countByMemberId(memberId);
        long weeklyExpenseAmount = clothesRepository.sumWeeklyExpenseAmount(memberId, oneWeekAgo);

        long closetCount = closetRepository.countByMemberId(memberId);
        long storageCount = closetSectionRepository.countByMemberId(memberId);

        int weeklyClosetUsageRate = calculateWeeklyClosetUsageRate(
                totalClothesCount,
                closetCount,
                storageCount
        );

        return new HomeSummaryResDto(
                totalClothesCount,
                weeklyExpenseAmount,
                weeklyClosetUsageRate,
                closetCount,
                storageCount
        );
    }

    private int calculateWeeklyClosetUsageRate(
            long totalClothesCount,
            long closetCount,
            long storageCount
    ) {
        if (totalClothesCount == 0 || closetCount == 0 || storageCount == 0) {
            return 0;
        }

        return TEMP_WEEKLY_CLOSET_USAGE_RATE;
    }
}