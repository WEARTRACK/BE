package com.weartrack.backend.domain.closet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "closet_section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClosetSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closet_id", nullable = false)
    private Closet closet;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(name = "section_name", nullable = false, length = 30)
    private String sectionName;

    public void setCloset(Closet closet) {
        this.closet = closet;
    }
}
