package com.weartrack.backend.domain.clothes.repository;

import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClothesPhotoRepository extends JpaRepository<ClothesPhoto, Long> {

    Optional<ClothesPhoto> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByMemberIdAndSourceUrl(Long memberId, String sourceUrl);
}
