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
        if (isSameOrSubdomain(normalizedHost, "musinsa.com")) {
            return SourceShop.MUSINSA;
        }
        if (isSameOrSubdomain(normalizedHost, "a-bly.com")
                || isSameOrSubdomain(normalizedHost, "ably.team")) {
            return SourceShop.ABLY;
        }
        if (isSameOrSubdomain(normalizedHost, "zigzag.kr")) {
            return SourceShop.ZIGZAG;
        }
        return SourceShop.UNKNOWN;
    }

    private boolean isSameOrSubdomain(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }
}
