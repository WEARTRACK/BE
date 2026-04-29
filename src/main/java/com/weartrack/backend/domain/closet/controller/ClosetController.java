package com.weartrack.backend.domain.closet.controller;

import com.weartrack.backend.domain.closet.dto.ClosetCreateReqDto;
import com.weartrack.backend.domain.closet.dto.ClosetCreateResDto;
import com.weartrack.backend.domain.closet.service.ClosetService;
import com.weartrack.backend.global.response.ApiResponse;
import com.weartrack.backend.global.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Closet", description = "옷장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closets")
public class ClosetController {

    private final ClosetService closetService;

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
}