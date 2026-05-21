package com.weartrack.backend.domain.closet.entity;

import com.weartrack.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "closet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Closet extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closet_id")
    private Long closetId;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "template_id", nullable = false)
    private Integer templateId;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClosetSection> sections = new ArrayList<>();

    public void addSection(ClosetSection section) {
        sections.add(section);
        section.setCloset(this);
    }
}
