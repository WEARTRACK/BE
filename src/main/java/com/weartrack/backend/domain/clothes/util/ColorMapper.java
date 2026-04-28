package com.weartrack.backend.domain.clothes.util;

import java.util.Map;

public class ColorMapper {

    private static final Map<String, String> COLOR_MAP = Map.ofEntries(
            Map.entry("검정", "black"),
            Map.entry("하양", "white"),
            Map.entry("흰색", "white"),
            Map.entry("회색", "gray"),
            Map.entry("빨강", "red"),
            Map.entry("주황", "orange"),
            Map.entry("노랑", "yellow"),
            Map.entry("초록", "green"),
            Map.entry("녹색", "green"),
            Map.entry("파랑", "blue"),
            Map.entry("남색", "navy"),
            Map.entry("보라", "purple"),
            Map.entry("분홍", "pink"),
            Map.entry("핑크", "pink"),
            Map.entry("갈색", "brown"),
            Map.entry("베이지", "beige")
    );

    private ColorMapper() {
    }

    public static String toEnglish(String koreanColor) {
        if (koreanColor == null || koreanColor.isBlank()) {
            return "unknown";
        }

        return COLOR_MAP.getOrDefault(koreanColor.trim(), "unknown");
    }
}