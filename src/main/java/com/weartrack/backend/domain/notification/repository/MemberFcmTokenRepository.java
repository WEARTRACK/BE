package com.weartrack.backend.domain.notification.repository;

import com.weartrack.backend.domain.notification.entity.MemberFcmToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    Optional<MemberFcmToken> findByToken(String token);

    List<MemberFcmToken> findAllByMemberId(Long memberId);

    List<MemberFcmToken> findAllByMemberIdIn(Collection<Long> memberIds);

    void deleteByMemberIdAndToken(Long memberId, String token);
}
