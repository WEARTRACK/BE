package com.weartrack.backend.domain.notification.entity;

import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "notification_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_setting_member",
                columnNames = "member_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "daily_review_enabled", nullable = false)
    private boolean dailyReviewEnabled;

    @Column(name = "long_unworn_clothes_enabled", nullable = false)
    private boolean longUnwornClothesEnabled;

    @Column(name = "fashion_report_enabled", nullable = false)
    private boolean fashionReportEnabled;

    @Builder
    private NotificationSetting(
            Long memberId,
            Boolean pushEnabled,
            Boolean dailyReviewEnabled,
            Boolean longUnwornClothesEnabled,
            Boolean fashionReportEnabled
    ) {
        this.memberId = memberId;
        this.pushEnabled = pushEnabled == null || pushEnabled;
        this.dailyReviewEnabled = dailyReviewEnabled == null || dailyReviewEnabled;
        this.longUnwornClothesEnabled = longUnwornClothesEnabled == null || longUnwornClothesEnabled;
        this.fashionReportEnabled = fashionReportEnabled == null || fashionReportEnabled;
    }

    public static NotificationSetting defaultFor(Long memberId) {
        return NotificationSetting.builder()
                .memberId(memberId)
                .build();
    }

    public void update(
            Boolean pushEnabled,
            Boolean dailyReviewEnabled,
            Boolean longUnwornClothesEnabled,
            Boolean fashionReportEnabled
    ) {
        if (pushEnabled != null) {
            this.pushEnabled = pushEnabled;
        }
        if (dailyReviewEnabled != null) {
            this.dailyReviewEnabled = dailyReviewEnabled;
        }
        if (longUnwornClothesEnabled != null) {
            this.longUnwornClothesEnabled = longUnwornClothesEnabled;
        }
        if (fashionReportEnabled != null) {
            this.fashionReportEnabled = fashionReportEnabled;
        }
    }

    public boolean isEnabled(NotificationType type) {
        if (!pushEnabled) {
            return false;
        }

        return switch (type) {
            case DAILY_REVIEW -> dailyReviewEnabled;
            case LONG_UNWORN_CLOTHES -> longUnwornClothesEnabled;
            case FASHION_REPORT -> fashionReportEnabled;
        };
    }
}
