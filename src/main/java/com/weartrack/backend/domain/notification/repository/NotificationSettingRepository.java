package com.weartrack.backend.domain.notification.repository;

import com.weartrack.backend.domain.notification.entity.NotificationSetting;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByMemberId(Long memberId);

    List<NotificationSetting> findAllByMemberIdIn(Collection<Long> memberIds);
}
