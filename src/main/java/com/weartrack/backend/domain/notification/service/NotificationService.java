package com.weartrack.backend.domain.notification.service;

import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.notification.dto.request.FcmTokenDeleteReqDto;
import com.weartrack.backend.domain.notification.dto.request.FcmTokenRegisterReqDto;
import com.weartrack.backend.domain.notification.dto.request.NotificationSettingUpdateReqDto;
import com.weartrack.backend.domain.notification.dto.response.NotificationSettingResDto;
import com.weartrack.backend.domain.notification.entity.MemberFcmToken;
import com.weartrack.backend.domain.notification.entity.NotificationSetting;
import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.domain.notification.repository.MemberFcmTokenRepository;
import com.weartrack.backend.domain.notification.repository.NotificationSettingRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final MemberRepository memberRepository;
    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void registerFcmToken(Long memberId, FcmTokenRegisterReqDto request) {
        MemberFcmToken token = memberFcmTokenRepository.findByToken(request.token())
                .orElseGet(() -> MemberFcmToken.builder()
                        .memberId(memberId)
                        .token(request.token())
                        .deviceType(request.deviceType())
                        .build());

        token.updateOwner(memberId, request.deviceType());
        memberFcmTokenRepository.save(token);
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

    public List<String> findTokensEnabledFor(NotificationType type) {
        List<Long> memberIds = memberRepository.findAll()
                .stream()
                .map(member -> member.getMemberId())
                .toList();

        return findTokensEnabledFor(type, memberIds);
    }

    public List<String> findTokensEnabledFor(NotificationType type, List<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return List.of();
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
            return List.of();
        }

        return memberFcmTokenRepository.findAllByMemberIdIn(enabledMemberIds)
                .stream()
                .map(MemberFcmToken::getToken)
                .distinct()
                .toList();
    }

    @Transactional
    public NotificationSetting getOrCreateSetting(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.defaultFor(memberId)
                ));
    }
}
