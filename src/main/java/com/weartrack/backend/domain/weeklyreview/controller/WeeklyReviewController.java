package com.weartrack.backend.domain.weeklyreview.controller;

import com.weartrack.backend.domain.weeklyreview.dto.response.WeeklyReviewSummaryResDto;
import com.weartrack.backend.domain.weeklyreview.service.WeeklyReviewService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weekly-reviews")
@Tag(name = "Weekly Review", description = "일간 회고 기반 주간 회고 결과 API")
public class WeeklyReviewController {

    private final WeeklyReviewService weeklyReviewService;

    @Operation(
            summary = "이번 주 회고 결과 조회",
            description = """
                    매일 저장한 일간 착용 기록을 현재 주차 기준으로 집계합니다.
                    주간 회고 결과 화면에 필요한 착용 옷 수, 옷장 활용률, 카테고리별 착용 옷 목록을 반환합니다.
                    """
    )
    @GetMapping("/current")
    public ApiResponse<WeeklyReviewSummaryResDto> getCurrentReview(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                weeklyReviewService.getCurrentReviewSummary(principal.memberId())
        );
    }

    @Operation(
            summary = "특정 주차 회고 결과 조회",
            description = """
                    특정 주차의 일간 착용 기록을 집계합니다.
                    weekStartDate는 해당 주의 시작일(일요일)을 yyyy-MM-dd 형식으로 전달합니다.
                    """
    )
    @GetMapping("/{weekStartDate}")
    public ApiResponse<WeeklyReviewSummaryResDto> getReview(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable LocalDate weekStartDate
    ) {
        return ApiResponse.success(
                weeklyReviewService.getReviewSummary(principal.memberId(), weekStartDate)
        );
    }
}
