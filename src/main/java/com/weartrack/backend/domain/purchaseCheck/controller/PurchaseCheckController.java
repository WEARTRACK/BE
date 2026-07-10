package com.weartrack.backend.domain.purchaseCheck.controller;

import com.weartrack.backend.domain.purchaseCheck.dto.request.PurchaseCheckLinkReqDto;
import com.weartrack.backend.domain.purchaseCheck.dto.response.PurchaseCheckResDto;
import com.weartrack.backend.domain.purchaseCheck.service.PurchaseCheckService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "PurchaseCheck", description = "구매 전 중복 옷 확인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchase-check")
public class PurchaseCheckController {

    private final PurchaseCheckService purchaseCheckService;

    @Operation(
            summary = "사진으로 구매 전 중복 옷 확인",
            description = """
                    업로드한 사진을 AI로 분석해 색상과 카테고리를 추출합니다.
                    추출된 색상/카테고리와 같은 기존 옷을 반환합니다.
                    """
    )
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PurchaseCheckResDto> checkByPhoto(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestPart("image") MultipartFile image,
            @RequestParam int page, @RequestParam int size
    ) {
        return ApiResponse.success(purchaseCheckService.checkByPhoto(
                principal.memberId(),
                image,
                page,
                size
        ));
    }

    @Operation(
            summary = "링크로 구매 전 중복 옷 확인",
            description = """
                    쇼핑몰 링크를 분석해 색상과 카테고리를 추출합니다.
                    추출된 색상/카테고리와 같은 기존 옷을 반환합니다.
                    """
    )
    @PostMapping("/link")
    public ApiResponse<PurchaseCheckResDto> checkByLink(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody PurchaseCheckLinkReqDto request,
            @RequestParam int page, @RequestParam int size
    ) {
        return ApiResponse.success(purchaseCheckService.checkByLink(
                principal.memberId(),
                request,
                page,
                size
        ));
    }
}
