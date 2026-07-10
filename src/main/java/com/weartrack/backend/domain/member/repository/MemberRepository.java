package com.weartrack.backend.domain.member.repository;

import com.weartrack.backend.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 회원 엔티티 접근을 담당한다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByMemberIdAndDeletedAtIsNull(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m
        FROM Member m
        WHERE m.memberId = :memberId
        """)
    Optional<Member> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
