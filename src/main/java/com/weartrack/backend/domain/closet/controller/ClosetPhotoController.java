package com.weartrack.backend.domain.closet.controller;

import com.weartrack.backend.domain.closet.dto.response.ClosetPhotoCreateResDto;
import com.weartrack.backend.domain.closet.service.ClosetPhotoService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Closet", description = "옷장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closets")
public class ClosetPhotoController {

    private final ClosetPhotoService closetPhotoService;

    @Operation(
            summary = "옷장 사진 AI 분석",
            description = "옷장 사진을 업로드하면 AI가 칸 개수를 인식하고 추천 템플릿 ID 목록을 반환합니다."
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ClosetPhotoCreateResDto> uploadClosetPhoto(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestPart("image") MultipartFile image
    ) {
        ClosetPhotoCreateResDto response = closetPhotoService.uploadClosetPhoto(
                principal.memberId(),
                image
        );

        return ApiResponse.success(response);
    }
}