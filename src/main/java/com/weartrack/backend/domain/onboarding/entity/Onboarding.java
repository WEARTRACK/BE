package com.weartrack.backend.domain.onboarding.entity;

import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "onboarding")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Onboarding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_id")
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OnboardingStatus status;

    @Column(name = "hidden", nullable = false)
    private boolean hidden;

    @Builder
    private Onboarding(Long memberId) {
        this.memberId = memberId;
        this.status = OnboardingStatus.IN_PROGRESS;
        this.hidden = false;
    }

    public static Onboarding create(Long memberId) {
        return Onboarding.builder()
                .memberId(memberId)
                .build();
    }

    public void complete() {
        this.status = OnboardingStatus.COMPLETED;
        this.hidden = false;
    }

    public void hide() {
        this.hidden = true;
    }

    public void showAgain() {
        this.hidden = false;
    }

    public boolean isCompleted() {
        return this.status == OnboardingStatus.COMPLETED;
    }
}