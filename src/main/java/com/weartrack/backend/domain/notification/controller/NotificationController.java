package com.weartrack.backend.domain.notification.controller;

import com.weartrack.backend.domain.notification.dto.request.FcmTokenDeleteReqDto;
import com.weartrack.backend.domain.notification.dto.request.FcmTokenRegisterReqDto;
import com.weartrack.backend.domain.notification.dto.request.NotificationSettingUpdateReqDto;
import com.weartrack.backend.domain.notification.dto.response.NotificationListResDto;
import com.weartrack.backend.domain.notification.dto.response.NotificationResDto;
import com.weartrack.backend.domain.notification.dto.response.NotificationSettingResDto;
import com.weartrack.backend.domain.notification.service.NotificationService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "푸시 알림 토큰, 알림 설정 및 알림 목록 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 목록 조회",
            description = """
                    알림 목록을 조회합니다.
                    최신 발송 알림이 먼저 오도록 정렬되며 각 알림의 읽음 여부를 함께 반환합니다.
                    """
    )
    @GetMapping
    public ApiResponse<NotificationListResDto> getNotifications(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(notificationService.getNotifications(
                principal.memberId(),
                pageable
        ));
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "알림을 읽음 상태로 변경합니다."
    )
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResDto> markAsRead(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long notificationId
    ) {
        return ApiResponse.success(notificationService.markAsRead(
                principal.memberId(),
                notificationId
        ));
    }

    @Operation(
            summary = "FCM 토큰 등록",
            description = """
                    FCM 디바이스 토큰을 등록합니다.
                    같은 토큰이 이미 등록되어 있으면 현재 사용자 기준으로 소유자와 기기 타입을 갱신합니다.
                    """
    )
    @PostMapping("/fcm-token")
    public ApiResponse<Void> registerFcmToken(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody FcmTokenRegisterReqDto request
    ) {
        notificationService.registerFcmToken(principal.memberId(), request);
        return ApiResponse.success();
    }

    @Operation(
            summary = "FCM 토큰 삭제",
            description = """
                    등록된 FCM 디바이스 토큰을 삭제합니다.
                    로그아웃하거나 더 이상 해당 기기로 푸시를 받지 않을 때 호출합니다.
                    """
    )
    @DeleteMapping("/fcm-token")
    public ApiResponse<Void> deleteFcmToken(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody FcmTokenDeleteReqDto request
    ) {
        notificationService.deleteFcmToken(principal.memberId(), request);
        return ApiResponse.success();
    }

    @Operation(
            summary = "알림 설정 조회",
            description = """
                    푸시 알림 수신 설정을 조회합니다.
                    별도 저장된 설정이 없는 경우 모든 알림이 ON인 기본 설정을 반환합니다.
                    """
    )
    @GetMapping("/settings")
    public ApiResponse<NotificationSettingResDto> getSettings(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(notificationService.getSetting(principal.memberId()));
    }

    @Operation(
            summary = "알림 설정 변경",
            description = """
                    푸시 알림 수신 설정을 변경합니다.
                    pushEnabled가 false이면 개별 알림 설정과 관계없이 모든 푸시 알림 발송이 중단됩니다.
                    """
    )
    @PatchMapping("/settings")
    public ApiResponse<NotificationSettingResDto> updateSettings(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody NotificationSettingUpdateReqDto request
    ) {
        return ApiResponse.success(notificationService.updateSetting(principal.memberId(), request));
    }
}
