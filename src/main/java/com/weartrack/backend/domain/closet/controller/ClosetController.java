package com.weartrack.backend.domain.closet.controller;

import com.weartrack.backend.domain.closet.dto.request.ClosetCreateReqDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetCreateResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetInquireResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetSelectResDto;
import com.weartrack.backend.domain.closet.dto.response.ClosetStatisticsDto;
import com.weartrack.backend.domain.closet.service.ClosetService;
import com.weartrack.backend.domain.clothes.dto.response.ClothesListResDto;
import com.weartrack.backend.domain.clothes.service.ClothesService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Closet", description = "옷장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closets")
public class ClosetController {

    private final ClosetService closetService;
    private final ClothesService clothesService;

    @Operation(
            summary = "옷장 등록",
            description = "templateId + imageUrl + 사용자가 입력한 칸 이름을 저장"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<ClosetCreateResDto> createCloset(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ClosetCreateReqDto request
    ) {

        ClosetCreateResDto response = closetService.createCloset(
                principal.memberId(),
                request
        );

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "디지털 옷장 조회",
            description = "closetId을 입력하여 내 옷장을 조회"
    )
    @GetMapping("/{closetId}")
    public ApiResponse<ClosetInquireResDto> getCloset(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @PathVariable Long closetId
    ) {
        ClosetInquireResDto response = closetService.getCloset(
                principal.memberId(), closetId);

        return ApiResponse.success(response);
    }


    @Operation(
            summary = "특정 칸 조회",
            description = "closetId + sectionId을 입력하여 특정 칸에 저장된 옷들을 조회"
    )
    @GetMapping("/{closetId}/sections/{sectionId}/clothes")
    public ApiResponse<ClothesListResDto> getClothesList(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @PathVariable Long closetId, @PathVariable Long sectionId,
            @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        ClothesListResDto response = closetService.getClothesBySection(
                principal.memberId(), closetId, sectionId, pageable
        );

        return ApiResponse.success(response);
    }


    @Operation(
            summary = "옷장 통계 조회",
            description = "closetId을 입력하여 옷장의 통계를 조회"
    )
    @GetMapping("/{closetId}/statistics")
    public ApiResponse<ClosetStatisticsDto> getStatistics(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @PathVariable Long closetId
    ){
        return ApiResponse.success(closetService.getStatistics(principal.memberId(), closetId));
    }

    @Operation(
            summary = "옷 등록용 내 옷장 선택 목록 조회",
            description = "옷 등록 화면에서 선택할 수 있는 내 옷장과 칸 목록을 조회"
    )
    @GetMapping("/select")
    public ApiResponse<List<ClosetSelectResDto>> getMyClosetsForSelect(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.success(
                closetService.getMyClosetsForSelect(principal.memberId())
        );
    }
}