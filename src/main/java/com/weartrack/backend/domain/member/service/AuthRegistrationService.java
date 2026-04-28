package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.member.repository.SocialAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 로그인 최초 진입 시 회원 생성을 담당한다.
 */
@Service
public class AuthRegistrationService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    public AuthRegistrationService(
            MemberRepository memberRepository,
            SocialAccountRepository socialAccountRepository
    ) {
        this.memberRepository = memberRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    /**
     * 신규 회원과 소셜 계정 연동 정보를 함께 저장한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Member registerNewMember(SocialUserInfo socialUserInfo) {
        Member member = memberRepository.save(Member.createPendingProfile());
        socialAccountRepository.saveAndFlush(SocialAccount.of(
                member,
                socialUserInfo.provider(),
                socialUserInfo.providerUserId(),
                socialUserInfo.email()
        ));
        return member;
    }
}
