package com.weartrack.backend.domain.home.controller;

import com.weartrack.backend.domain.home.dto.HomeSummaryResDto;
import com.weartrack.backend.domain.home.service.HomeService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "메인 홈 관련 API")
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "메인 홈 조회",
            description = "메인 홈 화면에 필요한 전체 옷 개수, 최근 1주일 소비 금액, 옷장 활용률을 조회합니다."
    )
    @GetMapping("/api/home")
    public ApiResponse<HomeSummaryResDto> getHomeSummary(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        HomeSummaryResDto response = homeService.getHomeSummary(principal.memberId());

        return ApiResponse.success(response);
    }
}