package com.weartrack.backend.domain.member.repository;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 소셜 계정 연동 정보 접근을 담당한다.
 */
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /**
     * 제공자와 제공자 사용자 식별자로 소셜 계정을 조회한다.
     */
    Optional<SocialAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
