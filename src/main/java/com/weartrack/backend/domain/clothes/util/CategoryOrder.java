package com.weartrack.backend.domain.clothes.util;

import java.util.Comparator;
import java.util.List;

public final class CategoryOrder {

    private static final List<String> ORDERED_CATEGORIES = List.of(
            "T-SHIRT",
            "SHIRT",
            "KNIT",
            "HOODIE",
            "VEST",
            "CARDIGAN",
            "PANTS",
            "SHORTS",
            "SKIRT",
            "DRESS",
            "JACKET",
            "COAT",
            "PADDING"
    );

    private CategoryOrder() {
    }

    public static List<String> orderedCategories() {
        return ORDERED_CATEGORIES;
    }

    public static Comparator<String> comparator() {
        return Comparator
                .comparingInt(CategoryOrder::orderIndex)
                .thenComparing(CategoryOrder::normalize);
    }

    public static int orderIndex(String category) {
        String normalizedCategory = normalize(category);
        int index = ORDERED_CATEGORIES.indexOf(normalizedCategory);

        if (index >= 0) {
            return index;
        }

        return ORDERED_CATEGORIES.size();
    }

    public static String normalize(String category) {
        if (category == null) {
            return "";
        }

        return category.trim()
                .toUpperCase()
                .replace(" ", "-")
                .replace("HODDIE", "HOODIE");
    }
}
