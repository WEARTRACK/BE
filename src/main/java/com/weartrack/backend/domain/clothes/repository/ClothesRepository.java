package com.weartrack.backend.domain.clothes.repository;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothesRepository extends JpaRepository<Clothes, Long> {

    @Query("""
        SELECT COUNT(c)
        FROM Clothes c
        JOIN ClothesPhoto cp ON c.clothesPhotoId = cp.id
        WHERE cp.memberId = :memberId
        """)
    long countByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT COALESCE(SUM(c.price), 0L)
        FROM Clothes c
        JOIN ClothesPhoto cp ON c.clothesPhotoId = cp.id
        WHERE cp.memberId = :memberId
          AND c.createdAt >= :startDate
        """)
    long sumWeeklyExpenseAmount(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate
    );

    Page<Clothes> findByClosetSectionIdOrderByCreatedAtDesc(Long closetSectionId, Pageable pageable);
}