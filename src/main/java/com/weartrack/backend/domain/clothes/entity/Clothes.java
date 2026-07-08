package com.weartrack.backend.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "brand_name", length = 100)
    private String brandName;

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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProductName(String productName) {
        if (productName != null) {
            this.productName = productName;
        }
    }

    public void updateBrandName(String brandName) {
        if (brandName != null) {
            this.brandName = brandName;
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