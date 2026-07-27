package com.weartrack.backend.domain.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class AppleSocialLoginProviderClient implements SocialLoginProviderClient {

    private static final String APPLE_KEYS_URI = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final Duration JWKS_CACHE_TTL = Duration.ofHours(6);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();
    private volatile Instant publicKeyCacheExpiresAt = Instant.EPOCH;

    public AppleSocialLoginProviderClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${APPLE_CLIENT_ID:com.weartrack.app}") String clientId
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.APPLE;
    }

    @Override
    public SocialUserInfo getUserInfo(String authorizationCode, String state) {
        throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_LOGIN_REQUEST);
    }

    @Override
    public SocialUserInfo getUserInfoByIdToken(String idToken) {
        try {
            String[] tokenParts = splitIdToken(idToken);
            JsonNode header = decodeJson(tokenParts[0]);
            JsonNode payload = decodeJson(tokenParts[1]);

            validateHeader(header);
            PublicKey publicKey = findPublicKey(header);
            verifySignature(tokenParts, publicKey);
            validatePayload(payload);

            return new SocialUserInfo(
                    supports(),
                    getRequiredText(payload, "sub"),
                    getOptionalText(payload, "email")
            );
        } catch (GeneralException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("Apple 공개키 조회에 실패했습니다.", e);
            throw new GeneralException(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE);
        } catch (Exception e) {
            log.warn("Apple ID 토큰 검증 중 예상하지 못한 오류가 발생했습니다.", e);
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private String[] splitIdToken(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String[] tokenParts = idToken.split("\\.");
        if (tokenParts.length != 3) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
        return tokenParts;
    }

    private JsonNode decodeJson(String base64UrlValue) throws java.io.IOException {
        byte[] decoded = Base64.getUrlDecoder().decode(base64UrlValue);
        return objectMapper.readTree(decoded);
    }

    private void validateHeader(JsonNode header) {
        if (!"RS256".equals(getRequiredText(header, "alg"))) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
        getRequiredText(header, "kid");
    }

    private PublicKey findPublicKey(JsonNode header) throws Exception {
        String keyId = getRequiredText(header, "kid");
        PublicKey cachedPublicKey = getCachedPublicKey(keyId);
        if (cachedPublicKey != null) {
            return cachedPublicKey;
        }

        refreshPublicKeyCache();
        PublicKey refreshedPublicKey = publicKeyCache.get(keyId);
        if (refreshedPublicKey != null) {
            return refreshedPublicKey;
        }

        throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
    }

    private PublicKey getCachedPublicKey(String keyId) {
        if (publicKeyCacheExpiresAt.isAfter(Instant.now())) {
            return publicKeyCache.get(keyId);
        }
        return null;
    }

    private synchronized void refreshPublicKeyCache() throws Exception {
        JsonNode keysBody = restClient.get()
                .uri(APPLE_KEYS_URI)
                .retrieve()
                .body(JsonNode.class);

        JsonNode keys = keysBody == null ? null : keysBody.path("keys");
        if (keys == null || !keys.isArray()) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        Map<String, PublicKey> refreshedKeys = new ConcurrentHashMap<>();
        for (JsonNode key : keys) {
            String keyId = getOptionalText(key, "kid");
            if (keyId != null) {
                refreshedKeys.put(keyId, createPublicKey(key));
            }
        }

        publicKeyCache.clear();
        publicKeyCache.putAll(refreshedKeys);
        publicKeyCacheExpiresAt = Instant.now().plus(JWKS_CACHE_TTL);
    }

    private PublicKey createPublicKey(JsonNode key) throws Exception {
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(getRequiredText(key, "n")));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(getRequiredText(key, "e")));
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(publicKeySpec);
    }

    private void verifySignature(String[] tokenParts, PublicKey publicKey) throws Exception {
        byte[] signedContent = (tokenParts[0] + "." + tokenParts[1]).getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = Base64.getUrlDecoder().decode(tokenParts[2]);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(signedContent);

        if (!signature.verify(signatureBytes)) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private void validatePayload(JsonNode payload) {
        if (!APPLE_ISSUER.equals(getRequiredText(payload, "iss"))) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        if (!clientId.equals(getRequiredText(payload, "aud"))) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        long expiresAt = payload.path("exp").asLong(0);
        if (expiresAt <= Instant.now().getEpochSecond()) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private String getRequiredText(JsonNode body, String fieldName) {
        if (body == null || body.path(fieldName).asText().isBlank()) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
        return body.path(fieldName).asText();
    }

    private String getOptionalText(JsonNode body, String fieldName) {
        if (body == null || body.path(fieldName).isMissingNode() || body.path(fieldName).asText().isBlank()) {
            return null;
        }
        return body.path(fieldName).asText();
    }
}
