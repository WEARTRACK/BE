package com.weartrack.backend.domain.clothes.link.parser;

import com.weartrack.backend.domain.clothes.entity.SourceShop;
import com.weartrack.backend.domain.clothes.link.ProductParseResult;
import com.weartrack.backend.domain.clothes.link.ProductParser;
import org.springframework.stereotype.Component;

@Component
public class CommonOgParser implements ProductParser {

    @Override
    public boolean supports(SourceShop sourceShop) {
        return true;
    }

    @Override
    public ProductParseResult parse(String html, String pageUrl) {
        return new ProductParseResult(
                HtmlMetaExtractor.findMetaContent(html, "og:title").orElse(null),
                HtmlMetaExtractor.findMetaContent(html, "og:image").orElse(null),
                HtmlMetaExtractor.findMetaContent(html, "og:description").orElse(null),
                HtmlMetaExtractor.findMetaContent(html, "og:url").orElse(pageUrl),
                HtmlMetaExtractor.findMetaContent(html, "product:price:amount")
                        .map(this::parsePrice)
                        .orElse(null),
                HtmlMetaExtractor.findMetaContent(html, "product:brand").orElse(null),
                null,
                null
        );
    }

    private Integer parsePrice(String priceText) {
        try {
            String digitsOnly = priceText.replaceAll("[^0-9]", "");
            return digitsOnly.isBlank() ? null : Integer.parseInt(digitsOnly);
        } catch (Exception e) {
            return null;
        }
    }
}
