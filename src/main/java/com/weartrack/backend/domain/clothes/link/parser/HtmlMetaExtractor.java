package com.weartrack.backend.domain.clothes.link.parser;

import org.springframework.web.util.HtmlUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HtmlMetaExtractor {

    private static final Pattern META_TAG_PATTERN = Pattern.compile("<meta\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "([a-zA-Z_:.-]+)\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))",
            Pattern.CASE_INSENSITIVE
    );

    private HtmlMetaExtractor() {
    }

    static Optional<String> findMetaContent(String html, String key) {
        Matcher tagMatcher = META_TAG_PATTERN.matcher(html);
        while (tagMatcher.find()) {
            String tag = tagMatcher.group();
            String property = findAttribute(tag, "property").orElse(null);
            String name = findAttribute(tag, "name").orElse(null);
            if (matchesKey(property, key) || matchesKey(name, key)) {
                return findAttribute(tag, "content").map(HtmlUtils::htmlUnescape);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> findAttribute(String tag, String attributeName) {
        Matcher attrMatcher = ATTR_PATTERN.matcher(tag);
        while (attrMatcher.find()) {
            if (attrMatcher.group(1).equalsIgnoreCase(attributeName)) {
                String value = firstNonNull(attrMatcher.group(3), attrMatcher.group(4), attrMatcher.group(5));
                return Optional.ofNullable(value);
            }
        }
        return Optional.empty();
    }

    private static boolean matchesKey(String value, String key) {
        return value != null && value.toLowerCase(Locale.ROOT).equals(key.toLowerCase(Locale.ROOT));
    }

    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
