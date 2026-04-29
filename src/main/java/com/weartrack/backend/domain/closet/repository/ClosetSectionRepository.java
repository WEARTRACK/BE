package com.weartrack.backend.domain.closet.repository;

import com.weartrack.backend.domain.closet.entity.ClosetSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetSectionRepository extends JpaRepository<ClosetSection, Long> {
}