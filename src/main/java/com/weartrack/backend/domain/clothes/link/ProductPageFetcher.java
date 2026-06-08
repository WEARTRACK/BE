package com.weartrack.backend.domain.clothes.link;

import com.weartrack.backend.domain.clothes.exception.ClothesErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPageFetcher {

    private static final int MAX_HTML_BYTES = 1_000_000;
    private static final int MAX_REDIRECTS = 3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final UrlSafetyValidator urlSafetyValidator;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public ProductPage fetch(String url) {
        String currentUrl = url;
        for (int redirectCount = 0; redirectCount <= MAX_REDIRECTS; redirectCount++) {
            urlSafetyValidator.validate(currentUrl);
            HttpResponse<InputStream> response = send(currentUrl);

            int statusCode = response.statusCode();
            if (statusCode >= 300 && statusCode < 400) {
                currentUrl = resolveRedirectUrl(currentUrl, response);
                continue;
            }
            if (statusCode < 200 || statusCode >= 300) {
                throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
            }

            return new ProductPage(currentUrl, readLimitedHtml(response));
        }

        throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
    }

    private HttpResponse<InputStream> send(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (Exception e) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
        }
    }

    private String resolveRedirectUrl(String currentUrl, HttpResponse<InputStream> response) {
        Optional<String> location = response.headers().firstValue("Location");
        if (location.isEmpty()) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
        }
        return URI.create(currentUrl).resolve(location.get()).toString();
    }

    private String readLimitedHtml(HttpResponse<InputStream> response) {
        try (InputStream inputStream = response.body();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int totalBytes = 0;
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) != -1) {
                totalBytes += readBytes;
                if (totalBytes > MAX_HTML_BYTES) {
                    throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
                }
                outputStream.write(buffer, 0, readBytes);
            }

            return outputStream.toString(resolveCharset(response));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ClothesErrorCode.PRODUCT_LINK_FETCH_FAILED);
        }
    }

    private Charset resolveCharset(HttpResponse<InputStream> response) {
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        for (String part : contentType.split(";")) {
            String trimmedPart = part.trim();
            if (trimmedPart.toLowerCase().startsWith("charset=")) {
                String charsetValue = trimmedPart.substring("charset=".length())
                        .trim()
                        .replace("\"", "")
                        .replace("'", "");
                if (charsetValue.isEmpty()) {
                    return StandardCharsets.UTF_8;
                }
                try {
                    return Charset.forName(charsetValue);
                } catch (Exception ignored) {
                    return StandardCharsets.UTF_8;
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}
