package com.weartrack.backend.domain.clothes.controller;

import com.weartrack.backend.domain.clothes.dto.request.ClothesFromLinkRequest;
import com.weartrack.backend.domain.clothes.dto.request.ProductLinkPreviewRequest;
import com.weartrack.backend.domain.clothes.dto.response.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.dto.response.ProductLinkPreviewResponse;
import com.weartrack.backend.domain.clothes.service.ProductLinkService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ProductLinkController {

    private final ProductLinkService productLinkService;

    @PostMapping("/link-preview")
    public ApiResponse<ProductLinkPreviewResponse> preview(
            @Valid @RequestBody ProductLinkPreviewRequest request
    ) {
        return ApiResponse.success(productLinkService.preview(request));
    }

    @PostMapping("/from-link")
    public ApiResponse<ClothesCreateResponse> createFromLink(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ClothesFromLinkRequest request
    ) {
        return ApiResponse.success(productLinkService.createFromLink(principal.memberId(), request));
    }
}
