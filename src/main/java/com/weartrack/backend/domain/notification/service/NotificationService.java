package com.weartrack.backend.domain.notification.service;

import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.notification.dto.request.FcmTokenDeleteReqDto;
import com.weartrack.backend.domain.notification.dto.request.FcmTokenRegisterReqDto;
import com.weartrack.backend.domain.notification.dto.request.NotificationSettingUpdateReqDto;
import com.weartrack.backend.domain.notification.dto.response.NotificationListResDto;
import com.weartrack.backend.domain.notification.dto.response.NotificationSettingResDto;
import com.weartrack.backend.domain.notification.entity.Notification;
import com.weartrack.backend.domain.notification.entity.NotificationSetting;
import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.domain.notification.repository.MemberFcmTokenRepository;
import com.weartrack.backend.domain.notification.repository.NotificationRepository;
import com.weartrack.backend.domain.notification.repository.NotificationSettingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final MemberRepository memberRepository;
    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void registerFcmToken(Long memberId, FcmTokenRegisterReqDto request) {
        memberFcmTokenRepository.upsertToken(
                memberId,
                request.token(),
                request.deviceTypeOrUnknown().name()
        );
        getOrCreateSetting(memberId);
    }

    @Transactional
    public void deleteFcmToken(Long memberId, FcmTokenDeleteReqDto request) {
        memberFcmTokenRepository.deleteByMemberIdAndToken(memberId, request.token());
    }

    @Transactional
    public NotificationSettingResDto updateSetting(
            Long memberId,
            NotificationSettingUpdateReqDto request
    ) {
        NotificationSetting setting = getOrCreateSetting(memberId);
        setting.update(
                request.pushEnabled(),
                request.dailyReviewEnabled(),
                request.longUnwornClothesEnabled(),
                request.fashionReportEnabled()
        );

        return NotificationSettingResDto.from(setting);
    }

    public NotificationSettingResDto getSetting(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .map(NotificationSettingResDto::from)
                .orElseGet(() -> NotificationSettingResDto.from(
                        NotificationSetting.defaultFor(memberId)
                ));
    }

    @Transactional
    public NotificationListResDto getNotifications(Long memberId, int page, int size) {
        notificationRepository.markAllUnreadAsReadByMemberId(memberId, LocalDateTime.now());
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Notification> notificationPage = notificationRepository.findByMemberIdOrderByCreatedAtDescIdDesc(
                memberId,
                pageable
        );

        return NotificationListResDto.from(notificationPage);
    }

    @Transactional
    public void saveNotification(
            Long memberId,
            NotificationType type,
            String title,
            String body
    ) {
        notificationRepository.save(Notification.builder()
                .memberId(memberId)
                .type(type)
                .title(title)
                .body(body)
                .build());
    }

    @Transactional
    public void saveNotifications(
            Set<Long> memberIds,
            NotificationType type,
            String title,
            String body
    ) {
        List<Notification> notifications = memberIds.stream()
                .map(memberId -> Notification.builder()
                        .memberId(memberId)
                        .type(type)
                        .title(title)
                        .body(body)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    public List<String> findTokensEnabledFor(NotificationType type) {
        List<Long> memberIds = memberRepository.findAll()
                .stream()
                .map(member -> member.getMemberId())
                .toList();

        return findTokensEnabledFor(type, memberIds);
    }

    public Map<Long, List<String>> findTokenMapEnabledFor(NotificationType type) {
        List<Long> memberIds = memberRepository.findAll()
                .stream()
                .map(member -> member.getMemberId())
                .toList();

        return findTokenMapEnabledFor(type, memberIds);
    }

    public List<String> findTokensEnabledFor(NotificationType type, List<Long> memberIds) {
        return findTokenMapEnabledFor(type, memberIds)
                .values()
                .stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
    }

    public Map<Long, List<String>> findTokenMapEnabledFor(
            NotificationType type,
            List<Long> memberIds
    ) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, NotificationSetting> settingMap = notificationSettingRepository
                .findAllByMemberIdIn(memberIds)
                .stream()
                .collect(Collectors.toMap(NotificationSetting::getMemberId, Function.identity()));

        Set<Long> enabledMemberIds = memberIds.stream()
                .filter(memberId -> settingMap
                        .getOrDefault(memberId, NotificationSetting.defaultFor(memberId))
                        .isEnabled(type))
                .collect(Collectors.toSet());

        if (enabledMemberIds.isEmpty()) {
            return Map.of();
        }

        return memberFcmTokenRepository.findAllByMemberIdIn(enabledMemberIds)
                .stream()
                .collect(Collectors.groupingBy(
                        token -> token.getMemberId(),
                        Collectors.mapping(
                                token -> token.getToken(),
                                Collectors.collectingAndThen(
                                        Collectors.toSet(),
                                        tokens -> tokens.stream().toList()
                                )
                        )
                ));
    }

    @Transactional
    public NotificationSetting getOrCreateSetting(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> createSettingOrFindExisting(memberId));
    }

    private NotificationSetting createSettingOrFindExisting(Long memberId) {
        try {
            return notificationSettingRepository.saveAndFlush(
                    NotificationSetting.defaultFor(memberId)
            );
        } catch (DataIntegrityViolationException e) {
            return notificationSettingRepository.findByMemberId(memberId)
                    .orElseThrow(() -> e);
        }
    }
}
