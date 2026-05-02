package com.weartrack.backend.domain.closet.entity;

import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
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

    @Column(name = "clothes_count")
    @Max(25)
    private Integer clothesCount = 0;


    public void setCloset(Closet closet) {
        this.closet = closet;
    }

    public void increaseClothesCount() {
        if (this.clothesCount >= 25) {
            throw new GeneralException(ClosetErrorCode.SECTION_FULL);
        }
        this.clothesCount++;
    }

    public void decreaseClothesCount() {
        if (this.clothesCount > 0) {
            this.clothesCount--;
        }
    }
}
