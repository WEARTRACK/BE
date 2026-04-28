package com.weartrack.backend.domain.member.repository;

import com.weartrack.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 엔티티 접근을 담당한다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);
}
