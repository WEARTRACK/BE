package com.weartrack.backend.domain.member.controller;

import com.weartrack.backend.domain.member.dto.request.NicknameSetReqDto;
import com.weartrack.backend.domain.member.dto.response.NicknameAvailabilityCheckResDto;
import com.weartrack.backend.domain.member.dto.response.NicknameSetResDto;
import com.weartrack.backend.domain.member.service.MemberService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 닉네임 중복 여부를 확인합니다.
     */
    @GetMapping("/nickname/check")
    public ApiResponse<NicknameAvailabilityCheckResDto> checkNicknameAvailability(
            @RequestParam
            @NotBlank
            @Size(max = 30)
            String nickname
    ) {
        return ApiResponse.success(memberService.checkNicknameAvailability(nickname));
    }

    /**
     * 현재 로그인한 사용자의 닉네임을 설정합니다.
     */
    @PatchMapping("/me/nickname")
    public ApiResponse<NicknameSetResDto> setNickname(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody NicknameSetReqDto request
    ) {
        return ApiResponse.success(memberService.setNickname(principal.memberId(), request));
    }
}
