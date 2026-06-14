package com.weartrack.backend.domain.onboarding.repository;

import com.weartrack.backend.domain.onboarding.entity.Onboarding;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {

    Optional<Onboarding> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}