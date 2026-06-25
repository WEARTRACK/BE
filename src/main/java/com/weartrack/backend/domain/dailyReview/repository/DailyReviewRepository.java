package com.weartrack.backend.domain.dailyReview.repository;

import com.weartrack.backend.domain.dailyReview.entity.DailyReview;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReviewRepository extends JpaRepository<DailyReview, Long> {

    Optional<DailyReview> findByMemberIdAndReviewDate(Long memberId, LocalDate reviewDate);

    boolean existsByMemberIdAndReviewDateBefore(Long memberId, LocalDate reviewDate);

    List<DailyReview> findAllByMemberIdAndReviewDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
}
