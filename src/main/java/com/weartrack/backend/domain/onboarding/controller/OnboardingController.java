package com.weartrack.backend.domain.onboarding.controller;

import com.weartrack.backend.domain.onboarding.dto.response.OnboardingQuestsResDto;
import com.weartrack.backend.domain.onboarding.dto.response.OnboardingSkipResDto;
import com.weartrack.backend.domain.onboarding.dto.response.OnboardingStatusResDto;
import com.weartrack.backend.domain.onboarding.service.OnboardingService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Onboarding", description = "퀘스트형 온보딩 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(
            summary = "온보딩 퀘스트 목록 조회",
            description = "로그인한 사용자의 온보딩 퀘스트 목록과 진행도를 조회합니다."
    )
    @GetMapping("/quests")
    public ApiResponse<OnboardingQuestsResDto> getQuests(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                onboardingService.getQuests(principal.memberId())
        );
    }

    @Operation(
            summary = "온보딩 완료 상태 조회",
            description = "로그인한 사용자의 온보딩 완료 여부를 조회합니다."
    )
    @GetMapping("/status")
    public ApiResponse<OnboardingStatusResDto> getStatus(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                onboardingService.getStatus(principal.memberId())
        );
    }

    @Operation(
            summary = "온보딩 스킵",
            description = "사용자가 온보딩을 건너뛰는 경우 스킵 상태로 처리합니다."
    )
    @PatchMapping("/skip")
    public ApiResponse<OnboardingSkipResDto> skip(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                onboardingService.skip(principal.memberId())
        );
    }
}