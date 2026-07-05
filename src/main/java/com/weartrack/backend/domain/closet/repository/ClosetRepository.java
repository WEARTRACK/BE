package com.weartrack.backend.domain.closet.repository;

import com.weartrack.backend.domain.closet.entity.Closet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    long countByMemberId(Long memberId);

    List<Closet> findAllByMemberId(Long memberId);
}