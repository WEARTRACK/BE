package com.weartrack.backend.domain.clothes.repository;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
          AND c.deletedAt IS NULL
        """)
    long countByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT COALESCE(SUM(c.price), 0L)
        FROM Clothes c
        JOIN ClothesPhoto cp ON c.clothesPhotoId = cp.id
        WHERE cp.memberId = :memberId
          AND c.createdAt >= :startDate
          AND c.deletedAt IS NULL
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
        AND c.deletedAt IS NULL
    """)
    List<Clothes> findAllByClosetId(@Param("closetId") Long closetId);

    @Query("""
        SELECT c FROM Clothes c
        WHERE c.closetSectionId IN (
            SELECT s.sectionId FROM ClosetSection s
            WHERE s.closet.memberId = :memberId
        )
        AND c.deletedAt IS NULL
        ORDER BY c.category ASC, c.createdAt DESC
        """)
    List<Clothes> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT c FROM Clothes c
        WHERE c.closetSectionId IN (
            SELECT s.sectionId FROM ClosetSection s
            WHERE s.closet.memberId = :memberId
        )
        ORDER BY c.category ASC, c.createdAt DESC
        """)
    List<Clothes> findAllIncludingDeletedByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT c FROM Clothes c
        WHERE c.id = :clothesId
          AND c.deletedAt IS NULL
        """)
    Optional<Clothes> findActiveById(@Param("clothesId") Long clothesId);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Clothes c
        JOIN ClothesPhoto cp ON c.clothesPhotoId = cp.id
        WHERE cp.memberId = :memberId
          AND cp.sourceUrl = :sourceUrl
          AND c.deletedAt IS NULL
        """)
    boolean existsActiveClothesByMemberIdAndSourceUrl(
            @Param("memberId") Long memberId,
            @Param("sourceUrl") String sourceUrl
    );

    @Query("""
        SELECT COUNT(c) FROM Clothes c
        WHERE c.id IN :clothesIds
          AND c.deletedAt IS NULL
          AND c.closetSectionId IN (
              SELECT s.sectionId FROM ClosetSection s
              WHERE s.closet.memberId = :memberId
          )
        """)
    long countOwnedClothesByIds(
            @Param("memberId") Long memberId,
            @Param("clothesIds") List<Long> clothesIds
    );

    Page<Clothes> findByClosetSectionIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long closetSectionId,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM Clothes c
    WHERE c.closetSectionId IN (
        SELECT s.sectionId FROM ClosetSection s
        WHERE s.closet.memberId = :memberId
    )
    AND c.deletedAt IS NULL
    AND (:color IS NULL OR c.color = :color)
    AND (:category IS NULL OR REPLACE(REPLACE(UPPER(c.category), '-', '_'), ' ', '_') = :category)
    """)
    Page<Clothes> searchByMemberIdAndFilters(
            @Param("memberId") Long memberId,
            @Param("color") String color,
            @Param("category") String category,
            Pageable pageable
    );
}
