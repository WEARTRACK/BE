package com.weartrack.backend.domain.dailyReview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "daily_review_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_review_item_review_clothes",
                columnNames = {"daily_review_id", "clothes_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReviewItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_review_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_review_id", nullable = false)
    private DailyReview dailyReview;

    @Column(name = "clothes_id", nullable = false)
    private Long clothesId;

    @Builder
    private DailyReviewItem(DailyReview dailyReview, Long clothesId) {
        this.dailyReview = dailyReview;
        this.clothesId = clothesId;
    }
}
