package com.weartrack.backend.domain.clothes.controller;

import com.weartrack.backend.domain.clothes.dto.request.ClothesCreateRequest;
import com.weartrack.backend.domain.clothes.dto.response.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.dto.response.ClothesFilterResDto;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Clothes", description = "옷 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

    private final ClothesService clothesService;

    @Operation(
            summary = "옷 추가 정보 등록",
            description = """
                    로그인한 사용자가 업로드한 photoId를 기반으로
                    확정한 색상, 카테고리, 가격, 보관 위치 정보를 저장합니다.
                    """
    )
    @PostMapping
    public ApiResponse<ClothesCreateResponse> createClothes(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ClothesCreateRequest request
    ) {
        ClothesCreateResponse response =
                clothesService.createClothes(principal.memberId(), request);

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "옷 필터 검색",
            description = "색상 또는 카테고리로 옷 검색. 둘 중 하나만 보내거나 둘 다 보낼 수 있음."
    )
    @GetMapping("/filter")
    public ApiResponse<ClothesFilterResDto> filterClothes(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(
                clothesService.filterClothes(principal.memberId(), color, category, pageable)
        );
    }
}