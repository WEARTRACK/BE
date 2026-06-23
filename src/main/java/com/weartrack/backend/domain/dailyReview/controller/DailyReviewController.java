package com.weartrack.backend.domain.dailyReview.controller;

import com.weartrack.backend.domain.dailyReview.dto.request.DailyReviewSaveReqDto;
import com.weartrack.backend.domain.dailyReview.dto.response.DailyReviewEntryResDto;
import com.weartrack.backend.domain.dailyReview.dto.response.DailyReviewSaveResDto;
import com.weartrack.backend.domain.dailyReview.service.DailyReviewService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-reviews")
@Tag(name = "Daily Review", description = "일간 착용 옷 회고 API")
public class DailyReviewController {

    private final DailyReviewService dailyReviewService;

    @Operation(
            summary = "오늘 입은 옷 선택 화면 조회",
            description = """
                    매일 20시 푸시 또는 홈 화면 진입 시 오늘 입은 옷을 선택하기 위한 API입니다.
                    """
    )
    @GetMapping("/today")
    public ApiResponse<DailyReviewEntryResDto> getTodayReview(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                dailyReviewService.getTodayReview(principal.memberId())
        );
    }

    @Operation(
            summary = "특정 날짜 입은 옷 선택 상태 조회",
            description = """
                    특정 날짜에 저장된 일간 회고 선택 상태를 조회합니다.
                    reviewDate는 yyyy-MM-dd 형식으로 전달합니다.
                    """
    )
    @GetMapping("/{reviewDate}")
    public ApiResponse<DailyReviewEntryResDto> getReview(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable LocalDate reviewDate
    ) {
        return ApiResponse.success(
                dailyReviewService.getReview(principal.memberId(), reviewDate)
        );
    }

    @Operation(
            summary = "오늘 입은 옷 저장",
            description = """
                    사용자가 오늘 입은 옷 목록을 저장합니다.
                    """
    )
    @PostMapping("/{reviewDate}")
    public ApiResponse<DailyReviewSaveResDto> saveReview(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable LocalDate reviewDate,
            @Valid @RequestBody DailyReviewSaveReqDto request
    ) {
        return ApiResponse.success(
                dailyReviewService.saveReview(principal.memberId(), reviewDate, request)
        );
    }
}
