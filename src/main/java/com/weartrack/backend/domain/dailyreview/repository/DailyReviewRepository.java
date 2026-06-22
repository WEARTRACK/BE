package com.weartrack.backend.domain.dailyreview.repository;

import com.weartrack.backend.domain.dailyreview.entity.DailyReview;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyReviewRepository extends JpaRepository<DailyReview, Long> {

    Optional<DailyReview> findByMemberIdAndReviewDate(Long memberId, LocalDate reviewDate);

    boolean existsByMemberIdAndReviewDateBefore(Long memberId, LocalDate reviewDate);

    List<DailyReview> findAllByMemberIdAndReviewDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT COUNT(DISTINCT item.clothesId)
        FROM DailyReview review
        JOIN review.items item
        WHERE review.memberId = :memberId
          AND review.reviewDate BETWEEN :startDate AND :endDate
          AND review.completed = true
        """)
    long countDistinctWornClothesByMemberIdAndReviewDateBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
