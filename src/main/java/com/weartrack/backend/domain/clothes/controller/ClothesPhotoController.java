package com.weartrack.backend.domain.clothes.controller;

import com.weartrack.backend.domain.clothes.dto.response.ClothesPhotoCreateResponse;
import com.weartrack.backend.domain.clothes.service.ClothesPhotoService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Clothes", description = "옷 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesPhotoController {

    private final ClothesPhotoService clothesPhotoService;

    @Operation(
            summary = "옷 사진 등록 및 AI 분석 요청",
            description = """
                    옷 사진을 업로드한 뒤 imageUrl과 photoId를 즉시 반환합니다.
                    AI 분석은 백그라운드에서 비동기로 처리되며,
                    최초 응답의 analysisStatus는 PENDING입니다.
                    분석 결과는 GET /api/clothes/photo/{photoId}/analysis API로 조회합니다.
                    """
    )
    @PostMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ClothesPhotoCreateResponse> uploadClothesPhoto(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestPart("image") MultipartFile image
    ) {
        Long memberId = principal.memberId();

        ClothesPhotoCreateResponse response =
                clothesPhotoService.uploadAndAnalyze(memberId, image);

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "옷 사진 AI 분석 상태 조회",
            description = """
                    업로드한 옷 사진의 AI 분석 상태를 조회합니다.
                    PENDING이면 아직 분석 중이고,
                    SUCCESS이면 predictedCategory와 predictedColor가 포함됩니다.
                    FAIL이면 분석에 실패한 상태입니다.
                    """
    )
    @GetMapping("/photo/{photoId}/analysis")
    public ApiResponse<ClothesPhotoCreateResponse> getClothesPhotoAnalysis(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long photoId
    ) {
        Long memberId = principal.memberId();

        ClothesPhotoCreateResponse response =
                clothesPhotoService.getAnalysisResult(memberId, photoId);

        return ApiResponse.success(response);
    }
}