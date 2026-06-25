package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.domain.closet.repository.ClosetSectionRepository;
import com.weartrack.backend.domain.clothes.dto.request.ClothesFromLinkRequest;
import com.weartrack.backend.domain.clothes.dto.request.ProductLinkPreviewRequest;
import com.weartrack.backend.domain.clothes.dto.response.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.dto.response.ProductLinkPreviewResponse;
import com.weartrack.backend.domain.clothes.entity.AnalysisStatus;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.entity.ClothesPhoto;
import com.weartrack.backend.domain.clothes.entity.ImageStorageType;
import com.weartrack.backend.domain.clothes.entity.SourceShop;
import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.domain.clothes.link.ProductPage;
import com.weartrack.backend.domain.clothes.link.ProductPageFetcher;
import com.weartrack.backend.domain.clothes.link.ProductParseResult;
import com.weartrack.backend.domain.clothes.link.ProductParser;
import com.weartrack.backend.domain.clothes.link.ProductUrlNormalizer;
import com.weartrack.backend.domain.clothes.link.SourceShopResolver;
import com.weartrack.backend.domain.clothes.link.UrlSafetyValidator;
import com.weartrack.backend.domain.clothes.link.parser.CommonOgParser;
import com.weartrack.backend.domain.clothes.link.parser.JsonLdParser;
import com.weartrack.backend.domain.clothes.repository.ClothesPhotoRepository;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductLinkService {

    private final ProductUrlNormalizer productUrlNormalizer;
    private final UrlSafetyValidator urlSafetyValidator;
    private final SourceShopResolver sourceShopResolver;
    private final ProductPageFetcher productPageFetcher;
    private final CommonOgParser commonOgParser;
    private final JsonLdParser jsonLdParser;
    private final List<ProductParser> productParsers;
    private final ClothesPhotoRepository clothesPhotoRepository;
    private final ClothesRepository clothesRepository;
    private final ClosetSectionRepository closetSectionRepository;

    public ProductLinkPreviewResponse preview(ProductLinkPreviewRequest request) {
        String normalizedUrl = productUrlNormalizer.normalize(request.url());
        SourceShop sourceShop = sourceShopResolver.resolve(normalizedUrl);

        ProductPage page = productPageFetcher.fetch(normalizedUrl);
        ProductParseResult result = parseProduct(sourceShop, page.html(), page.url());

        if (isBlank(result.productName()) || isBlank(result.imageUrl())) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_PARSE_FAILED);
        }

        return new ProductLinkPreviewResponse(
                sourceShop,
                normalizedUrl,
                result.productName(),
                result.imageUrl(),
                result.price(),
                result.brandName(),
                result.category(),
                result.color(),
                null
        );
    }

    @Transactional
    public ClothesCreateResponse createFromLink(Long memberId, ClothesFromLinkRequest request) {
        if (request.imageStorageType() != ImageStorageType.EXTERNAL_URL) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }

        String normalizedSourceUrl = productUrlNormalizer.normalize(request.sourceUrl());
        urlSafetyValidator.validate(request.imageUrl());
        SourceShop sourceShop = sourceShopResolver.resolve(normalizedSourceUrl);

        if (clothesRepository.existsActiveClothesByMemberIdAndSourceUrl(
                memberId,
                normalizedSourceUrl
        )) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_DUPLICATED);
        }

        ClosetSection section = closetSectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new GeneralException(ClosetErrorCode.SECTION_NOT_FOUND));

        if (!section.getCloset().getMemberId().equals(memberId)) {
            throw new GeneralException(ClosetErrorCode.SECTION_NOT_OWNED);
        }

        ClothesPhoto photo = clothesPhotoRepository.save(ClothesPhoto.builder()
                .memberId(memberId)
                .imageUrl(request.imageUrl())
                .imageStorageType(ImageStorageType.EXTERNAL_URL)
                .sourceShop(sourceShop)
                .sourceUrl(normalizedSourceUrl)
                .analysisStatus(AnalysisStatus.SKIPPED)
                .predictedCategory(request.category())
                .predictedColor(request.color())
                .build());

        Clothes clothes = Clothes.builder()
                .clothesPhotoId(photo.getId())
                .closetSectionId(section.getSectionId())
                .imageUrl(request.imageUrl())
                .productName(request.productName())
                .color(request.color())
                .category(request.category())
                .price(request.price())
                .purchaseDate(request.purchaseDate())
                .storageLocation(request.storageLocation())
                .build();

        Clothes savedClothes = clothesRepository.save(clothes);
        section.increaseClothesCount();

        return toCreateResponse(savedClothes);
    }

    private ProductParseResult parseProduct(SourceShop sourceShop, String html, String pageUrl) {
        ProductParseResult commonResult = commonOgParser.parse(html, pageUrl);
        ProductParseResult jsonLdResult = jsonLdParser.parse(html, pageUrl);
        ProductParseResult result = commonResult.merge(jsonLdResult);

        for (ProductParser parser : productParsers) {
            if (parser == commonOgParser || parser == jsonLdParser || !parser.supports(sourceShop)) {
                continue;
            }
            result = parser.parse(html, pageUrl).merge(result);
        }

        return result;
    }

    private ClothesCreateResponse toCreateResponse(Clothes clothes) {
        return new ClothesCreateResponse(
                clothes.getId(),
                clothes.getClothesPhotoId(),
                clothes.getImageUrl(),
                clothes.getProductName(),
                clothes.getColor(),
                clothes.getCategory(),
                clothes.getPrice(),
                clothes.getPurchaseDate(),
                clothes.getStorageLocation(),
                clothes.getClosetSectionId(),
                clothes.getCreatedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
