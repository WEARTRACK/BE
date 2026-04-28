package com.weartrack.backend.domain.clothes.controller;

import com.weartrack.backend.domain.clothes.dto.ClothesPhotoCreateResponse;
import com.weartrack.backend.domain.clothes.service.ClothesPhotoService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            summary = "옷 사진 등록 및 AI 분석",
            description = "옷 사진을 업로드하면 로컬에 저장하고 AI 서버를 호출하여 카테고리와 색상을 예측합니다."
    )
    @PostMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ClothesPhotoCreateResponse> uploadClothesPhoto(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestPart("image") MultipartFile image
    ) {
        Long userId = principal.memberId();

        ClothesPhotoCreateResponse response =
                clothesPhotoService.uploadAndAnalyze(userId, image);

        return ApiResponse.success(response);
    }
}