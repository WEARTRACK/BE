package com.weartrack.backend.domain.clothes.repository;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import java.time.LocalDateTime;
import java.util.List;

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

    @Query("""
        SELECT c FROM Clothes c
        WHERE c.closetSectionId IN (
        SELECT s.sectionId FROM ClosetSection s
        WHERE s.closet.closetId = :closetId
    )
    """)
    List<Clothes> findAllByClosetId(@Param("closetId") Long closetId);

    Page<Clothes> findByClosetSectionIdOrderByCreatedAtDesc(Long closetSectionId, Pageable pageable);

    @Query("""
    SELECT c FROM Clothes c
    WHERE c.closetSectionId IN (
        SELECT s.sectionId FROM ClosetSection s
        WHERE s.closet.memberId = :memberId
    )
    AND (:color IS NULL OR c.color = :color)
    AND (:category IS NULL OR c.category = :category)
    """)
    Page<Clothes> searchByMemberIdAndFilters(
            @Param("memberId") Long memberId,
            @Param("color") String color,
            @Param("category") String category,
            Pageable pageable
    );
}