package com.weartrack.backend.domain.clothes.link;

import com.weartrack.backend.domain.clothes.entity.SourceShop;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

@Component
public class SourceShopResolver {

    public SourceShop resolve(String url) {
        String host = URI.create(url).getHost();
        if (host == null) {
            return SourceShop.UNKNOWN;
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if (normalizedHost.contains("musinsa.com")) {
            return SourceShop.MUSINSA;
        }
        if (normalizedHost.contains("a-bly.com") || normalizedHost.contains("ably.team")) {
            return SourceShop.ABLY;
        }
        if (normalizedHost.contains("zigzag.kr")) {
            return SourceShop.ZIGZAG;
        }
        return SourceShop.UNKNOWN;
    }
}
