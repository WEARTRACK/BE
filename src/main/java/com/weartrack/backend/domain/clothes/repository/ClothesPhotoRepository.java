package com.weartrack.backend.domain.clothes.repository;

import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesPhotoRepository extends JpaRepository<ClothesPhoto, Long> {
}