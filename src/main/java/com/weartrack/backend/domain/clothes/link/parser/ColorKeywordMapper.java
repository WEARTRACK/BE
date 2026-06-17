package com.weartrack.backend.domain.clothes.link.parser;

import java.util.Map;

final class ColorKeywordMapper {

    private static final Map<String, String> COLOR_MAP = Map.ofEntries(
            Map.entry("\ube14\ub799", "Black"),
            Map.entry("black", "Black"),
            Map.entry("\ud654\uc774\ud2b8", "White"),
            Map.entry("white", "White"),
            Map.entry("\uc624\ud504\ud654\uc774\ud2b8", "White"),
            Map.entry("\uc544\uc774\ubcf4\ub9ac", "Beige"),
            Map.entry("ivory", "Beige"),
            Map.entry("\ud06c\ub9bc", "Beige"),
            Map.entry("cream", "Beige"),
            Map.entry("\uc624\ud2b8\ubc00", "Beige"),
            Map.entry("\ubca0\uc774\uc9c0", "Beige"),
            Map.entry("beige", "Beige"),
            Map.entry("\ube0c\ub77c\uc6b4", "Brown"),
            Map.entry("brown", "Brown"),
            Map.entry("\uadf8\ub808\uc774", "Gray"),
            Map.entry("gray", "Gray"),
            Map.entry("grey", "Gray"),
            Map.entry("\ucc28\ucf5c", "Gray"),
            Map.entry("\ub124\uc774\ube44", "Navy"),
            Map.entry("navy", "Navy"),
            Map.entry("\ube14\ub8e8", "Blue"),
            Map.entry("blue", "Blue"),
            Map.entry("\uc2a4\uce74\uc774", "Blue"),
            Map.entry("\uc778\ub514\uace0", "Blue"),
            Map.entry("\ub370\ub2d8", "Blue"),
            Map.entry("\ub808\ub4dc", "Red"),
            Map.entry("red", "Red"),
            Map.entry("\ud551\ud06c", "Pink"),
            Map.entry("pink", "Pink"),
            Map.entry("\uc610\ub85c\uc6b0", "Yellow"),
            Map.entry("yellow", "Yellow"),
            Map.entry("\uadf8\ub9b0", "Green"),
            Map.entry("green", "Green"),
            Map.entry("\uce74\ud0a4", "Green"),
            Map.entry("\ud37c\ud50c", "Purple"),
            Map.entry("purple", "Purple"),
            Map.entry("\uc624\ub80c\uc9c0", "Orange"),
            Map.entry("orange", "Orange")
    );

    private ColorKeywordMapper() {
    }

    static String infer(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.toLowerCase();
        return COLOR_MAP.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
