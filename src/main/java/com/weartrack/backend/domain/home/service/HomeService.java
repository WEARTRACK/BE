package com.weartrack.backend.domain.home.service;

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

    private static final int HARDCODED_WEEKLY_CLOSET_USAGE_RATE = 73;

    private final ClothesRepository clothesRepository;

    public HomeSummaryResDto getHomeSummary(Long memberId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        int totalClothesCount = clothesRepository.countByMemberId(memberId);
        int weeklyExpenseAmount = clothesRepository.sumWeeklyExpenseAmount(memberId, oneWeekAgo);

        return new HomeSummaryResDto(
                totalClothesCount,
                weeklyExpenseAmount,
                HARDCODED_WEEKLY_CLOSET_USAGE_RATE
        );
    }
}