package com.weartrack.backend.domain.clothes.link.parser;

import com.weartrack.backend.domain.clothes.entity.SourceShop;
import com.weartrack.backend.domain.clothes.link.ProductParseResult;
import com.weartrack.backend.domain.clothes.link.ProductParser;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AblyParser implements ProductParser {

    private static final Pattern WON_PRICE_PATTERN = Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})+)\\s*원");

    @Override
    public boolean supports(SourceShop sourceShop) {
        return sourceShop == SourceShop.ABLY;
    }

    @Override
    public ProductParseResult parse(String html, String pageUrl) {
        String title = HtmlMetaExtractor.findMetaContent(html, "og:title")
                .or(() -> HtmlMetaExtractor.findMetaContent(html, "twitter:title"))
                .orElse(null);
        String description = HtmlMetaExtractor.findMetaContent(html, "og:description")
                .or(() -> HtmlMetaExtractor.findMetaContent(html, "description"))
                .or(() -> HtmlMetaExtractor.findMetaContent(html, "twitter:description"))
                .orElse(null);

        return new ProductParseResult(
                cleanTitle(title),
                HtmlMetaExtractor.findMetaContent(html, "og:image")
                        .or(() -> HtmlMetaExtractor.findMetaContent(html, "twitter:image"))
                        .orElse(null),
                description,
                HtmlMetaExtractor.findMetaContent(html, "og:url").orElse(pageUrl),
                parsePrice(firstNonBlank(description, title)),
                null,
                null,
                ColorKeywordMapper.infer(firstNonBlank(title, description))
        );
    }

    private String cleanTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title
                .replaceAll("\\s*[-|]\\s*에이블리.*$", "")
                .trim();
    }

    private Integer parsePrice(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = WON_PRICE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1).replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
