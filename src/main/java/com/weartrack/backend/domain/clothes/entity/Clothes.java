package com.weartrack.backend.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "price")
    private Integer price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}