package com.weartrack.backend.domain.closet.entity;

import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ClosetTemplate {

    TEMPLATE_1(1, 7),
    TEMPLATE_2(2, 4),
    TEMPLATE_3(3, 4),
    TEMPLATE_4(4, 8),
    TEMPLATE_5(5, 10);

    private final Integer templateId;
    private final Integer sectionCount;

    ClosetTemplate(Integer templateId, Integer sectionCount) {
        this.templateId = templateId;
        this.sectionCount = sectionCount;
    }

    public static ClosetTemplate from(Integer templateId) {
        return Arrays.stream(values())
                .filter(template -> template.templateId.equals(templateId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.INVALID_TEMPLATE_ID));
    }

    public List<Integer> getSectionOrders() {
        return java.util.stream.IntStream.rangeClosed(1, sectionCount)
                .boxed()
                .toList();
    }
}