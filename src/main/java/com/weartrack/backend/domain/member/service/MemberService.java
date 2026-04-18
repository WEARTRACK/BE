package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.dto.response.NicknameAvailabilityCheckResDto;
import com.weartrack.backend.domain.member.dto.request.NicknameSetReqDto;
import com.weartrack.backend.domain.member.dto.response.NicknameSetResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.exception.MemberErrorCode;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.global.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 닉네임 중복 여부를 확인합니다.
     */
    public NicknameAvailabilityCheckResDto checkNicknameAvailability(String nickname) {
        boolean available = !memberRepository.existsByNickname(nickname);
        return new NicknameAvailabilityCheckResDto(nickname, available);
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

        return new NicknameSetResDto(
                member.getMemberId(),
                member.getNickname(),
                member.hasNickname()
        );
    }
}
