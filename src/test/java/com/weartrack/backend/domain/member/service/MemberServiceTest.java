package com.weartrack.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.weartrack.backend.domain.member.dto.request.NicknameSetReqDto;
import com.weartrack.backend.domain.member.dto.response.NicknameAvailabilityCheckResDto;
import com.weartrack.backend.domain.member.dto.response.NicknameSetResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.exception.MemberErrorCode;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.global.exception.GeneralException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("닉네임이 중복되지 않으면 사용 가능 응답을 반환한다.")
    void checkNicknameAvailability() {
        given(memberRepository.existsByNickname("weartrack")).willReturn(false);

        NicknameAvailabilityCheckResDto response = memberService.checkNicknameAvailability("weartrack");

        assertThat(response.nickname()).isEqualTo("weartrack");
        assertThat(response.available()).isTrue();
    }

    @Test
    @DisplayName("닉네임 설정 시 사용자를 찾으면 닉네임을 저장한다.")
    void setNickname() {
        Member member = Member.createPendingProfile();
        ReflectionTestUtils.setField(member, "memberId", 1L);

        given(memberRepository.existsByNickname("new-nickname")).willReturn(false);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        NicknameSetResDto response = memberService.setNickname(1L, new NicknameSetReqDto("new-nickname"));

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("new-nickname");
        assertThat(response.profileCompleted()).isTrue();
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 예외를 발생시킨다.")
    void setNicknameFailWhenAlreadyExists() {
        given(memberRepository.existsByNickname("duplicated")).willReturn(true);

        assertThatThrownBy(() -> memberService.setNickname(1L, new NicknameSetReqDto("duplicated")))
                .isInstanceOf(GeneralException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.NICKNAME_ALREADY_EXISTS);

        verify(memberRepository, never()).findById(any());
    }
}
