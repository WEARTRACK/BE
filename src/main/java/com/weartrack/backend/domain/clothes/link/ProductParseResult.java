package com.weartrack.backend.domain.clothes.link;

public record ProductParseResult(
        String productName,
        String imageUrl,
        String description,
        String canonicalUrl,
        Integer price,
        String brandName,
        String category,
        String color
) {
    public ProductParseResult merge(ProductParseResult fallback) {
        if (fallback == null) {
            return this;
        }

        return new ProductParseResult(
                firstNonBlank(productName, fallback.productName),
                firstNonBlank(imageUrl, fallback.imageUrl),
                firstNonBlank(description, fallback.description),
                firstNonBlank(canonicalUrl, fallback.canonicalUrl),
                price != null ? price : fallback.price,
                firstNonBlank(brandName, fallback.brandName),
                firstNonBlank(category, fallback.category),
                firstNonBlank(color, fallback.color)
        );
    }

    private static String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
