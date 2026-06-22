package com.weartrack.backend.domain.home.controller;

import com.weartrack.backend.domain.home.dto.HomeSummaryResDto;
import com.weartrack.backend.domain.home.dto.HomeWeeklyClosetUsageResDto;
import com.weartrack.backend.domain.home.service.HomeService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "홈 화면 조회",
            description = "홈 화면에 필요한 전체 옷 개수, 최근 1주일 소비 금액, 이번 주 옷장 활용률, 옷장/보관함 개수를 조회합니다."
    )
    @GetMapping("/api/home")
    public ApiResponse<HomeSummaryResDto> getHomeSummary(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        HomeSummaryResDto response = homeService.getHomeSummary(principal.memberId());

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "이번 주 옷장 활용률 상세 조회",
            description = """
                    홈 화면의 이번 주 옷장 활용률 카드를 눌렀을 때 호출하는 API입니다.
                    
                    weeklyClosetUsageRate는 홈 화면의 이번 주 옷장 활용률과 같은 계산식을 사용합니다.
                    공식은 (이번 주 입은 고유 옷 수 / 전체 옷 수) * 100 입니다.
                    
                    closetUsageMessage는 전체 옷 수에서 오늘 회고에서 선택한 옷 수를 뺀 값으로 생성합니다.
                    이번 주 입은 옷 목록과 그 옷들의 가격 총합도 함께 반환합니다.
                    """
    )
    @GetMapping("/api/home/weekly-closet-usage")
    public ApiResponse<HomeWeeklyClosetUsageResDto> getWeeklyClosetUsage(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        HomeWeeklyClosetUsageResDto response =
                homeService.getWeeklyClosetUsage(principal.memberId());

        return ApiResponse.success(response);
    }
}
