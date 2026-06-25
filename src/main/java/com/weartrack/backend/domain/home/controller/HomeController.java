package com.weartrack.backend.domain.home.controller;

import com.weartrack.backend.domain.home.dto.response.HomeSummaryResDto;
import com.weartrack.backend.domain.home.dto.response.HomeWeeklyClosetUsageAnalysisResDto;
import com.weartrack.backend.domain.home.dto.response.HomeWeeklyWornClothesResDto;
import com.weartrack.backend.domain.home.service.HomeService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "홈 화면 조회",
            description = "홈 화면에 필요한 전체 옷 개수, 최근 1주일 소비 금액, 이번 주 옷장 활용률, 옷장/보관함 개수를 조회합니다."
    )
    @GetMapping
    public ApiResponse<HomeSummaryResDto> getHomeSummary(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        HomeSummaryResDto response = homeService.getHomeSummary(principal.memberId());

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "이번 주 옷장 활용률 분석 조회",
            description = "홈 화면의 이번 주 옷장 활용률 카드를 눌렀을 때 옷장 분석 내용을 보여줍니다."
    )
    @GetMapping("/weekly-closet-usage/analysis")
    public ApiResponse<HomeWeeklyClosetUsageAnalysisResDto> getWeeklyClosetUsageAnalysis(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        HomeWeeklyClosetUsageAnalysisResDto response =
                homeService.getWeeklyClosetUsageAnalysis(principal.memberId());

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "이번 주 입은 옷 및 가격 조회",
            description = "옷장 활용률 분석 화면에서 분포 카드를 눌렀을 때 이번 주 입은 옷 목록과 가격 총합을 조회합니다."
    )
    @GetMapping("/weekly-closet-usage/worn-clothes")
    public ApiResponse<HomeWeeklyWornClothesResDto> getWeeklyWornClothes(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        HomeWeeklyWornClothesResDto response =
                homeService.getWeeklyWornClothes(principal.memberId());

        return ApiResponse.success(response);
    }
}
