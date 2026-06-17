package com.weartrack.backend.domain.clothes.link.parser;

import com.weartrack.backend.domain.clothes.entity.SourceShop;
import com.weartrack.backend.domain.clothes.link.ProductParseResult;
import com.weartrack.backend.domain.clothes.link.ProductParser;
import org.springframework.stereotype.Component;

@Component
public class ZigzagParser implements ProductParser {

    @Override
    public boolean supports(SourceShop sourceShop) {
        return sourceShop == SourceShop.ZIGZAG;
    }

    @Override
    public ProductParseResult parse(String html, String pageUrl) {
        String title = HtmlMetaExtractor.findMetaContent(html, "og:title")
                .or(() -> HtmlMetaExtractor.findMetaContent(html, "twitter:title"))
                .orElse(null);
        String description = HtmlMetaExtractor.findMetaContent(html, "og:description")
                .or(() -> HtmlMetaExtractor.findMetaContent(html, "description"))
                .orElse(null);

        return new ProductParseResult(
                cleanTitle(title),
                null,
                description,
                null,
                null,
                null,
                inferCategory(title),
                ColorKeywordMapper.infer(firstNonBlank(title, description))
        );
    }

    private String cleanTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }

    private String inferCategory(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        if (containsAny(title, "셔츠", "블라우스")) {
            return "Shirt";
        }
        if (containsAny(title, "티셔츠", "반팔", "긴팔", "슬리브")) {
            return "T-shirt";
        }
        if (containsAny(title, "가디건", "카디건")) {
            return "Cardigan";
        }
        if (containsAny(title, "니트", "스웨터")) {
            return "Knit";
        }
        if (containsAny(title, "후드")) {
            return "Hoodie";
        }
        if (containsAny(title, "자켓", "재킷", "점퍼")) {
            return "Jacket";
        }
        if (containsAny(title, "코트")) {
            return "Coat";
        }
        if (containsAny(title, "패딩")) {
            return "Padding";
        }
        if (containsAny(title, "팬츠", "바지", "슬랙스", "데님")) {
            return "Pants";
        }
        if (containsAny(title, "스커트", "치마")) {
            return "Skirt";
        }
        if (containsAny(title, "원피스", "드레스")) {
            return "Dress";
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
