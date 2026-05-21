package com.weartrack.backend.domain.closet.repository;

import com.weartrack.backend.domain.closet.entity.Closet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    long countByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}