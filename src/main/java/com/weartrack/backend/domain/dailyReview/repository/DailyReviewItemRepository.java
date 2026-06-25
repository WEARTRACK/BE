package com.weartrack.backend.domain.dailyReview.repository;

import com.weartrack.backend.domain.dailyReview.entity.DailyReviewItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReviewItemRepository extends JpaRepository<DailyReviewItem, Long> {
}
