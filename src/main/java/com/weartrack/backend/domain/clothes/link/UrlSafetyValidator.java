package com.weartrack.backend.domain.clothes.link;

import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;

@Component
public class UrlSafetyValidator {

    public void validate(String url) {
        URI uri = toUri(url);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if (normalizedHost.equals("localhost") || normalizedHost.endsWith(".localhost")) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_UNSUPPORTED_URL);
        }

        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(address)) {
                    throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_UNSUPPORTED_URL);
                }
            }
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
        }
    }

    private URI toUri(String url) {
        try {
            return URI.create(url);
        } catch (Exception e) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_INVALID_URL);
        }
    }

    private boolean isBlockedAddress(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || isCarrierGradeNat(address);
    }

    private boolean isCarrierGradeNat(InetAddress address) {
        if (!(address instanceof Inet4Address)) {
            return false;
        }
        byte[] bytes = address.getAddress();
        int first = bytes[0] & 0xff;
        int second = bytes[1] & 0xff;
        return first == 100 && second >= 64 && second <= 127;
    }
}
