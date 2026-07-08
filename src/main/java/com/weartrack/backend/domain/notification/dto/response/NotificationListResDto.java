package com.weartrack.backend.domain.notification.dto.response;

import com.weartrack.backend.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "알림 목록 응답")
public record NotificationListResDto(
        @Schema(description = "전체 알림 개수", example = "2")
        Integer totalCount,

        @Schema(description = "현재 페이지 번호. 0부터 시작합니다.", example = "0")
        Integer currentPage,

        @Schema(description = "전체 페이지 수", example = "1")
        Integer totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        Boolean hasNext,

        @Schema(description = "알림 목록")
        List<NotificationResDto> notifications
) {

    public static NotificationListResDto from(Page<Notification> page) {
        List<NotificationResDto> notifications = page.getContent().stream()
                .map(NotificationResDto::from)
                .toList();

        return new NotificationListResDto(
                (int) page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages(),
                page.hasNext(),
                notifications
        );
    }
}
