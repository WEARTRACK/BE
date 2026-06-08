package com.weartrack.backend.domain.clothes.link.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weartrack.backend.domain.clothes.entity.SourceShop;
import com.weartrack.backend.domain.clothes.link.ProductParseResult;
import com.weartrack.backend.domain.clothes.link.ProductParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JsonLdParser implements ProductParser {

    private static final Pattern JSON_LD_PATTERN = Pattern.compile(
            "<script[^>]+type\\s*=\\s*['\"]application/ld\\+json['\"][^>]*>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final int MAX_JSON_LD_DEPTH = 20;

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(SourceShop sourceShop) {
        return true;
    }

    @Override
    public ProductParseResult parse(String html, String pageUrl) {
        Matcher matcher = JSON_LD_PATTERN.matcher(html);
        while (matcher.find()) {
            ProductParseResult result = parseJsonLdBlock(matcher.group(1));
            if (result != null) {
                return result;
            }
        }
        return new ProductParseResult(null, null, null, null, null, null, null, null);
    }

    private ProductParseResult parseJsonLdBlock(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(HtmlUtils.htmlUnescape(rawJson).trim());
            return findProduct(root, 0);
        } catch (Exception e) {
            return null;
        }
    }

    private ProductParseResult findProduct(JsonNode node, int depth) {
        if (node == null || node.isNull() || depth > MAX_JSON_LD_DEPTH) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                ProductParseResult result = findProduct(child, depth + 1);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        if (node.has("@graph")) {
            ProductParseResult result = findProduct(node.get("@graph"), depth + 1);
            if (result != null) {
                return result;
            }
        }
        if (isProductNode(node)) {
            return fromProductNode(node);
        }
        return null;
    }

    private boolean isProductNode(JsonNode node) {
        JsonNode type = node.get("@type");
        if (type == null) {
            return false;
        }
        if (type.isArray()) {
            Iterator<JsonNode> iterator = type.elements();
            while (iterator.hasNext()) {
                if ("Product".equalsIgnoreCase(iterator.next().asText())) {
                    return true;
                }
            }
            return false;
        }
        return "Product".equalsIgnoreCase(type.asText());
    }

    private ProductParseResult fromProductNode(JsonNode node) {
        return new ProductParseResult(
                text(node, "name"),
                image(node.get("image")),
                text(node, "description"),
                text(node, "url"),
                price(node.get("offers")),
                brand(node.get("brand")),
                text(node, "category"),
                text(node, "color")
        );
    }

    private String image(JsonNode imageNode) {
        if (imageNode == null || imageNode.isNull()) {
            return null;
        }
        if (imageNode.isArray() && !imageNode.isEmpty()) {
            return image(imageNode.get(0));
        }
        if (imageNode.isObject()) {
            return text(imageNode, "url");
        }
        return imageNode.asText(null);
    }

    private Integer price(JsonNode offersNode) {
        if (offersNode == null || offersNode.isNull()) {
            return null;
        }
        if (offersNode.isArray() && !offersNode.isEmpty()) {
            return price(offersNode.get(0));
        }
        String priceText = text(offersNode, "price");
        if (priceText == null) {
            priceText = text(offersNode, "lowPrice");
        }
        if (priceText == null) {
            return null;
        }
        try {
            String digitsOnly = priceText.replaceAll("[^0-9]", "");
            return digitsOnly.isBlank() ? null : Integer.parseInt(digitsOnly);
        } catch (Exception e) {
            return null;
        }
    }

    private String brand(JsonNode brandNode) {
        if (brandNode == null || brandNode.isNull()) {
            return null;
        }
        if (brandNode.isObject()) {
            return text(brandNode, "name");
        }
        return brandNode.asText(null);
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        String value = node.get(fieldName).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
