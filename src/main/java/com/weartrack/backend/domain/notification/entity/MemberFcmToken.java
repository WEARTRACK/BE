package com.weartrack.backend.domain.notification.entity;

import com.weartrack.backend.domain.notification.entity.enums.DeviceType;
import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "member_fcm_token",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_fcm_token_token",
                columnNames = "token"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_fcm_token_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    @Builder
    private MemberFcmToken(Long memberId, String token, DeviceType deviceType) {
        this.memberId = memberId;
        this.token = token;
        this.deviceType = deviceType == null ? DeviceType.UNKNOWN : deviceType;
    }

    public void updateOwner(Long memberId, DeviceType deviceType) {
        this.memberId = memberId;
        this.deviceType = deviceType == null ? DeviceType.UNKNOWN : deviceType;
    }
}
