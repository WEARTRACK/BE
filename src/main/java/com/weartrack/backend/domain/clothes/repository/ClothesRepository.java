package com.weartrack.backend.domain.clothes.repository;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesRepository extends JpaRepository<Clothes, Long> {
}