package com.weartrack.backend.domain.member.entity;

import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "nickname", unique = true, length = 30)
    private String nickname;

    @OneToMany(mappedBy = "member")
    private final List<SocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    private Member(String nickname) {
        this.nickname = nickname;
    }

    public static Member createPendingProfile() {
        return Member.builder()
                .nickname(null)
                .build();
    }

    public boolean hasNickname() {
        return nickname != null && !nickname.isBlank();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
