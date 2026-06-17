package com.weartrack.backend.domain.closet.entity;

import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public enum ClosetTemplate {

    TEMPLATE_2(2),
    TEMPLATE_3(3),
    TEMPLATE_4(4),
    TEMPLATE_5(5),
    TEMPLATE_6(6),
    TEMPLATE_7(7),
    TEMPLATE_8(8),
    TEMPLATE_9(9),
    TEMPLATE_10(10),
    TEMPLATE_11(11);

    private final Integer templateId;
    private final Integer sectionCount;

    ClosetTemplate(Integer templateId) {
        this.templateId = templateId;
        this.sectionCount = templateId;
    }

    public static ClosetTemplate from(Integer templateId) {
        return Arrays.stream(values())
                .filter(template -> template.templateId.equals(templateId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.INVALID_TEMPLATE_ID));
    }

    public List<Integer> getSectionOrders() {
        return IntStream.rangeClosed(1, sectionCount)
                .boxed()
                .toList();
    }
}