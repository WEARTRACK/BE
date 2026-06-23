package com.weartrack.backend.domain.fashionReport.controller;

import com.weartrack.backend.domain.fashionReport.dto.response.WeeklyFashionConsumptionReportResDto;
import com.weartrack.backend.domain.fashionReport.service.FashionConsumptionReportService;
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

@Tag(name = "Fashion Consumption Report", description = "패션 소비 리포트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fashion-consumption/reports")
public class FashionConsumptionReportController {

    private final FashionConsumptionReportService fashionConsumptionReportService;

    @Operation(
            summary = "현재 주간 패션 소비 리포트 조회",
            description = """
                    홈 화면의 이번 주 총 패션 지출액 카드를 눌렀을 때 호출하는 API입니다.
                    현재 진행 중인 이번 주에 등록한 옷의 가격을 실시간 집계합니다.
                    """
    )
    @GetMapping("/weekly/current")
    public ApiResponse<WeeklyFashionConsumptionReportResDto> getCurrentWeeklyReport(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                fashionConsumptionReportService.getCurrentWeeklyReport(principal.memberId())
        );
    }

    @Operation(
            summary = "특정 주차 패션 소비 리포트 조회",
            description = """
                    특정 주차의 패션 소비 리포트를 조회합니다.
                    weekStartDate는 해당 주의 시작일(일요일)을 yyyy-MM-dd 형식으로 전달합니다.
                    """
    )
    @GetMapping("/weekly/{weekStartDate}")
    public ApiResponse<WeeklyFashionConsumptionReportResDto> getWeeklyReport(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable LocalDate weekStartDate
    ) {
        return ApiResponse.success(
                fashionConsumptionReportService.getWeeklyReport(
                        principal.memberId(),
                        weekStartDate
                )
        );
    }
}
