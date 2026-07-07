package com.weartrack.backend.domain.notification.repository;

import com.weartrack.backend.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberIdOrderByCreatedAtDescIdDesc(Long memberId, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true,
                n.readAt = :readAt
            WHERE n.memberId = :memberId
              AND n.read = false
            """)
    void markAllUnreadAsReadByMemberId(
            @Param("memberId") Long memberId,
            @Param("readAt") LocalDateTime readAt
    );
}
