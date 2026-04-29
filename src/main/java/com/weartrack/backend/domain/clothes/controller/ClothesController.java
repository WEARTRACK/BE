package com.weartrack.backend.domain.clothes.controller;

import com.weartrack.backend.domain.clothes.dto.ClothesCreateRequest;
import com.weartrack.backend.domain.clothes.dto.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.service.ClothesService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}