package com.weartrack.backend.domain.dailyreview.repository;

import com.weartrack.backend.domain.dailyreview.entity.DailyReviewItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReviewItemRepository extends JpaRepository<DailyReviewItem, Long> {
}
