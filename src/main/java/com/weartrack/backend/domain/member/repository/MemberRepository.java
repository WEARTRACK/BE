package com.weartrack.backend.domain.member.repository;

import com.weartrack.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);
}
