package com.weartrack.backend.domain.clothes.link.parser;

import com.weartrack.backend.domain.clothes.entity.SourceShop;
import com.weartrack.backend.domain.clothes.link.ProductParseResult;
import com.weartrack.backend.domain.clothes.link.ProductParser;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MusinsaParser implements ProductParser {

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("\uc81c\ud488\ubd84\ub958\\s*:\\s*(.*?)\\s+\ube0c\ub79c\ub4dc\\s*:");
    private static final Pattern BRAND_PATTERN = Pattern.compile("\ube0c\ub79c\ub4dc\\s*:\\s*(.*?)\\s+\uc81c\ud488\ubc88\ud638\\s*:");
    private static final Pattern PRODUCT_PATTERN = Pattern.compile("\uc81c\ud488\\s*:\\s*(.*?)(?:\\s+-\\s+[0-9,]+)?$");

    @Override
    public boolean supports(SourceShop sourceShop) {
        return sourceShop == SourceShop.MUSINSA;
    }

    @Override
    public ProductParseResult parse(String html, String pageUrl) {
        String description = HtmlMetaExtractor.findMetaContent(html, "og:description")
                .or(() -> HtmlMetaExtractor.findMetaContent(html, "description"))
                .orElse(null);
        String title = HtmlMetaExtractor.findMetaContent(html, "og:title").orElse(null);
        String productName = firstNonBlank(extract(description, PRODUCT_PATTERN), cleanTitle(title));
        String sourceCategory = extract(description, CATEGORY_PATTERN);

        return new ProductParseResult(
                productName,
                null,
                description,
                null,
                null,
                extract(description, BRAND_PATTERN),
                mapCategory(sourceCategory),
                ColorKeywordMapper.infer(join(productName, title, description))
        );
    }

    private String extract(String text, Pattern pattern) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1).trim();
        return value.isBlank() ? null : value;
    }

    private String cleanTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title
                .replaceAll("\\s+-\\s+\uc0ac\uc774\uc988.*$", "")
                .replaceAll("\\s+\\|\\s*\ubb34\uc2e0\uc0ac$", "")
                .trim();
    }

    private String mapCategory(String sourceCategory) {
        if (sourceCategory == null || sourceCategory.isBlank()) {
            return null;
        }
        if (containsAny(sourceCategory, "\ubc18\uc18c\ub9e4", "\ud2f0\uc154\uce20")) {
            return "T-shirt";
        }
        if (containsAny(sourceCategory, "\uc154\uce20", "\ube14\ub77c\uc6b0\uc2a4")) {
            return "Shirt";
        }
        if (containsAny(sourceCategory, "\uac00\ub514\uac74", "\uce74\ub514\uac74")) {
            return "Cardigan";
        }
        if (containsAny(sourceCategory, "\ub2c8\ud2b8", "\uc2a4\uc6e8\ud130")) {
            return "Knit";
        }
        if (containsAny(sourceCategory, "\ud6c4\ub4dc")) {
            return "Hoodie";
        }
        if (containsAny(sourceCategory, "\uc790\ucf13", "\uc7ac\ud0b7", "\uc810\ud37c")) {
            return "Jacket";
        }
        if (containsAny(sourceCategory, "\ucf54\ud2b8")) {
            return "Coat";
        }
        if (containsAny(sourceCategory, "\ud328\ub529")) {
            return "Padding";
        }
        if (containsAny(sourceCategory, "\ud32c\uce20", "\ubc14\uc9c0", "\ub370\ub2d8", "\uc2ac\ub799\uc2a4")) {
            return "Pants";
        }
        if (containsAny(sourceCategory, "\uc2a4\ucee4\ud2b8", "\uce58\ub9c8")) {
            return "Skirt";
        }
        if (containsAny(sourceCategory, "\uc6d0\ud53c\uc2a4", "\ub4dc\ub808\uc2a4")) {
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

    private String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                builder.append(value).append(' ');
            }
        }
        return builder.toString();
    }

    private String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
