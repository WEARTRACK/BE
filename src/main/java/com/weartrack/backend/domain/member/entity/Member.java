package com.weartrack.backend.domain.member.entity;

import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 기본 정보를 저장하는 엔티티다.
 */
@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "nickname", unique = true, length = 5)
    private String nickname;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "member")
    private final List<SocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    private Member(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 닉네임이 아직 없는 회원 엔티티를 만든다.
     */
    public static Member createPendingProfile() {
        return Member.builder()
                .nickname(null)
                .build();
    }

    /**
     * 닉네임 입력이 완료된 회원인지 확인한다.
     */
    public boolean hasNickname() {
        return nickname != null && !nickname.isBlank();
    }

    public boolean isWithdrawn() {
        return deletedAt != null;
    }

    public boolean isRejoinBlocked(LocalDateTime now, long blockDays) {
        return isWithdrawn() && deletedAt.plusDays(blockDays).isAfter(now);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void withdraw() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public void reactivate() {
        deletedAt = null;
    }
}
