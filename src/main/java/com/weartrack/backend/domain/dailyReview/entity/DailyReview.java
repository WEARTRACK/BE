package com.weartrack.backend.domain.dailyReview.entity;

import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "daily_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_review_member_date",
                columnNames = {"member_id", "review_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_review_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @OneToMany(mappedBy = "dailyReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DailyReviewItem> items = new ArrayList<>();

    @Builder
    private DailyReview(Long memberId, LocalDate reviewDate) {
        this.memberId = memberId;
        this.reviewDate = reviewDate;
        this.completed = false;
    }

    public void replaceItems(List<Long> clothesIds) {
        this.items.clear();
        clothesIds.forEach(clothesId ->
                this.items.add(DailyReviewItem.builder()
                        .dailyReview(this)
                        .clothesId(clothesId)
                        .build())
        );
        this.completed = true;
    }

    public void addItems(List<Long> clothesIds) {
        Set<Long> existingClothesIds = this.items.stream()
                .map(DailyReviewItem::getClothesId)
                .collect(Collectors.toSet());

        clothesIds.stream()
                .filter(clothesId -> !existingClothesIds.contains(clothesId))
                .forEach(clothesId -> this.items.add(DailyReviewItem.builder()
                        .dailyReview(this)
                        .clothesId(clothesId)
                        .build()));

        this.completed = true;
    }
}
