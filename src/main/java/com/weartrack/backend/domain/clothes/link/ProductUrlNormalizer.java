package com.weartrack.backend.domain.clothes.link;

import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class ProductUrlNormalizer {

    public String normalize(String rawUrl) {
        URI uri = toUri(rawUrl);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }
        if (uri.getHost() == null) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }

        try {
            return new URI(
                    scheme.toLowerCase(Locale.ROOT),
                    uri.getUserInfo(),
                    uri.getHost().toLowerCase(Locale.ROOT),
                    uri.getPort(),
                    uri.getRawPath(),
                    normalizeQuery(uri.getRawQuery()),
                    null
            ).toString();
        } catch (URISyntaxException e) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }
    }

    private URI toUri(String rawUrl) {
        try {
            return new URI(rawUrl.trim());
        } catch (Exception e) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }
    }

    private String normalizeQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        String query = Arrays.stream(rawQuery.split("&"))
                .filter(parameter -> !isTrackingParameter(parameter))
                .collect(Collectors.joining("&"));

        return query.isBlank() ? null : query;
    }

    private boolean isTrackingParameter(String parameter) {
        String key = parameter.split("=", 2)[0];
        String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8)
                .toLowerCase(Locale.ROOT);
        return decodedKey.startsWith("utm_")
                || decodedKey.equals("fbclid")
                || decodedKey.equals("gclid");
    }
}
