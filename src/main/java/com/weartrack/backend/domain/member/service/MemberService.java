package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.dto.request.NicknameSetReqDto;
import com.weartrack.backend.domain.member.dto.request.RequiredTermsAgreementReqDto;
import com.weartrack.backend.domain.member.dto.response.MemberMyPageResDto;
import com.weartrack.backend.domain.member.dto.response.NicknameAvailabilityCheckResDto;
import com.weartrack.backend.domain.member.dto.response.NicknameSetResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import com.weartrack.backend.domain.member.exception.MemberErrorCode;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.member.repository.SocialAccountRepository;
import com.weartrack.backend.global.exception.GeneralException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    public MemberService(
            MemberRepository memberRepository,
            SocialAccountRepository socialAccountRepository
    ) {
        this.memberRepository = memberRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    /**
     * 닉네임 중복 여부를 확인합니다.
     */
    public NicknameAvailabilityCheckResDto checkNicknameAvailability(String nickname) {
        boolean available = !memberRepository.existsByNickname(nickname);
        return new NicknameAvailabilityCheckResDto(nickname, available);
    }

    public MemberMyPageResDto getMyPageInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        String email = socialAccountRepository.findFirstByMemberMemberIdOrderBySocialAccountIdAsc(memberId)
                .map(SocialAccount::getProviderEmail)
                .orElse(null);

        return new MemberMyPageResDto(
                member.getMemberId(),
                member.getNickname(),
                email
        );
    }

    @Transactional
    /**
     * 로그인한 사용자의 닉네임을 설정합니다.
     */
    public NicknameSetResDto setNickname(Long memberId, NicknameSetReqDto request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new GeneralException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(request.nickname());
        flushNicknameChange(member);

        return new NicknameSetResDto(
                member.getMemberId(),
                member.getNickname(),
                member.hasNickname()
        );
    }

    @Transactional
    public void agreeRequiredTerms(Long memberId, RequiredTermsAgreementReqDto request) {
        if (!Boolean.TRUE.equals(request.requiredTermsAgreed())) {
            throw new GeneralException(MemberErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.agreeRequiredTerms();
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (member.isWithdrawn()) {
            throw new GeneralException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }

        member.withdraw();
    }

    private void flushNicknameChange(Member member) {
        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
