package com.weartrack.backend.domain.closet.repository;

import com.weartrack.backend.domain.closet.entity.ClosetSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClosetSectionRepository extends JpaRepository<ClosetSection, Long> {

    @Query("""
        SELECT COUNT(cs)
        FROM ClosetSection cs
        WHERE cs.closet.memberId = :memberId
        """)
    long countByMemberId(@Param("memberId") Long memberId);
}