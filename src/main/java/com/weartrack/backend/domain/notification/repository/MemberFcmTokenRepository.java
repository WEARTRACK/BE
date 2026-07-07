package com.weartrack.backend.domain.notification.repository;

import com.weartrack.backend.domain.notification.entity.MemberFcmToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    Optional<MemberFcmToken> findByToken(String token);

    List<MemberFcmToken> findAllByMemberId(Long memberId);

    List<MemberFcmToken> findAllByMemberIdIn(Collection<Long> memberIds);

    void deleteByMemberIdAndToken(Long memberId, String token);

    @Modifying
    @Query(value = """
            INSERT INTO member_fcm_token (
                member_id,
                token,
                device_type,
                created_at,
                updated_at
            )
            VALUES (
                :memberId,
                :token,
                :deviceType,
                NOW(6),
                NOW(6)
            )
            ON DUPLICATE KEY UPDATE
                member_id = VALUES(member_id),
                device_type = VALUES(device_type),
                updated_at = NOW(6)
            """, nativeQuery = true)
    void upsertToken(
            @Param("memberId") Long memberId,
            @Param("token") String token,
            @Param("deviceType") String deviceType
    );
}
