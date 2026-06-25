package com.weartrack.backend.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Clothes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clothes_id")
    private Long id;

    @Column(name = "clothes_photo_id", nullable = false)
    private Long clothesPhotoId;

    @Column(name = "closet_section_id", nullable = false)
    private Long closetSectionId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "price")
    private Integer price;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        // TODO: 서버 기본 타임존에 의존하지 않도록 생성·수정 시각을 Instant 또는 설정 타임존 기준으로 통일한다.
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateColor(String color) {
        if (color != null && !color.isBlank()) {
            this.color = color;
        }
    }

    public void updateCategory(String category) {
        if (category != null && !category.isBlank()) {
            this.category = category;
        }
    }

    public void updatePrice(Integer price) {
        if (price != null && price >= 0) {
            this.price = price;
        }
    }

    public void moveToSection(Long sectionId) {
        if (sectionId != null) {
            this.closetSectionId = sectionId;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
