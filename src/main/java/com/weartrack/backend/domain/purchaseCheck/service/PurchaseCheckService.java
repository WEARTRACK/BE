package com.weartrack.backend.domain.purchaseCheck.service;

import com.weartrack.backend.domain.clothes.dto.ResultDto;
import com.weartrack.backend.domain.clothes.dto.request.ProductLinkPreviewRequest;
import com.weartrack.backend.domain.clothes.dto.response.AiClothesPredictionResponse;
import com.weartrack.backend.domain.clothes.dto.response.ProductLinkPreviewResponse;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.clothes.service.ClothesAiClient;
import com.weartrack.backend.domain.clothes.service.ProductLinkService;
import com.weartrack.backend.domain.clothes.util.CategoryOrder;
import com.weartrack.backend.domain.clothes.util.ColorMapper;
import com.weartrack.backend.domain.purchaseCheck.dto.request.PurchaseCheckLinkReqDto;
import com.weartrack.backend.domain.purchaseCheck.dto.response.PurchaseCheckResDto;
import com.weartrack.backend.domain.purchaseCheck.entity.enums.PurchaseCheckResultType;
import com.weartrack.backend.global.exception.GeneralException;
import java.io.IOException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseCheckService {

    private final ClothesAiClient clothesAiClient;
    private final ProductLinkService productLinkService;
    private final ClothesRepository clothesRepository;

    public PurchaseCheckResDto checkByPhoto(
            Long memberId,
            MultipartFile image,
            int page,
            int size
    ) {
        Pageable pageable = createPageable(page, size);
        AiClothesPredictionResponse aiResult = predict(image);

        if (aiResult == null || aiResult.results() == null || aiResult.results().isEmpty()) {
            return emptyResponse(PurchaseCheckResultType.ANALYSIS_FAILED, pageable);
        }

        ResultDto firstResult = aiResult.results().get(0);

        String category = normalizeCategory(firstResult.category());
        String color = normalizeColor(firstResult.color());

        return findSimilarClothes(memberId, category, color, pageable);
    }

    public PurchaseCheckResDto checkByLink(
            Long memberId,
            PurchaseCheckLinkReqDto request,
            int page,
            int size
    ) {
        ProductLinkPreviewResponse preview =
                productLinkService.preview(new ProductLinkPreviewRequest(request.url()));

        String category = normalizeCategory(preview.category());
        String color = normalizeColor(preview.color());

        return findSimilarClothes(memberId, category, color, createPageable(page, size));
    }

    private AiClothesPredictionResponse predict(MultipartFile image) {
        try {
            return clothesAiClient.predict(
                    image.getBytes(),
                    image.getOriginalFilename(),
                    image.getContentType()
            );
        } catch (IOException e) {
            throw new GeneralException(ClothesErrorCode.CLOTHES_IMAGE_READ_FAILED);
        } catch (WebClientException e) {
            log.warn("구매 확인 AI 요청 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private PurchaseCheckResDto findSimilarClothes(
            Long memberId,
            String category,
            String color,
            Pageable pageable
    ) {
        if (clothesRepository.countByMemberId(memberId) == 0) {
            return emptyResponse(PurchaseCheckResultType.NO_REGISTERED_CLOTHES, pageable);
        }

        if (isBlank(category) || isBlank(color) || "unknown".equals(color)) {
            return emptyResponse(PurchaseCheckResultType.ANALYSIS_FAILED, pageable);
        }

        Page<Clothes> similarClothes = clothesRepository.findSimilarClothesForPurchaseCheck(
                memberId,
                color,
                category,
                pageable
        );

        String message = similarClothes.hasContent()
                ? PurchaseCheckResultType.HAS_SIMILAR_CLOTHES.message(similarClothes.getTotalElements())
                : PurchaseCheckResultType.NO_SIMILAR_CLOTHES.message();

        return PurchaseCheckResDto.from(message, similarClothes);
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 20));
    }

    private PurchaseCheckResDto emptyResponse(
            PurchaseCheckResultType resultType,
            Pageable pageable
    ) {
        return PurchaseCheckResDto.from(resultType.message(), Page.empty(pageable));
    }

    private String normalizeCategory(String category) {
        if (isBlank(category)) {
            return null;
        }

        return CategoryOrder.normalize(category);
    }

    private String normalizeColor(String color) {
        if (isBlank(color)) {
            return null;
        }

        String trimmedColor = color.trim();
        if (trimmedColor.chars().allMatch(this::isAsciiColorCharacter)) {
            return trimmedColor.toLowerCase(Locale.ROOT).replace("_", "-");
        }

        return ColorMapper.toEnglish(trimmedColor);
    }

    private boolean isAsciiColorCharacter(int ch) {
        return (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z')
                || ch == '_'
                || ch == '-';
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
