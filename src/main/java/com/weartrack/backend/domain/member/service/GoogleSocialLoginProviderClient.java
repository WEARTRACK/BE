package com.weartrack.backend.domain.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Google 소셜 로그인 연동을 처리한다.
 */
@Component
public class GoogleSocialLoginProviderClient implements SocialLoginProviderClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final GoogleIdTokenVerifier idTokenVerifier;

    public GoogleSocialLoginProviderClient(
            RestClient restClient,
            @Value("${GOOGLE_CLIENT_ID}") String clientId,
            @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret,
            @Value("${GOOGLE_REDIRECT_URI}") String redirectUri,
            @Value("${GOOGLE_IOS_CLIENT_ID:}") String iosClientId
    ) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        List<String> audiences = List.of(clientId, iosClientId).stream()
                .filter(StringUtils::hasText)
                .toList();
        this.idTokenVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(audiences)
                .build();
    }

    /**
     * 현재 구현체가 처리하는 제공자를 반환한다.
     */
    @Override
    public AuthProvider supports() {
        return AuthProvider.GOOGLE;
    }

    /**
     * Google 인가 코드를 access token으로 교환한 뒤 사용자 정보를 조회합니다.
     */
    @Override
    public SocialUserInfo getUserInfo(String authorizationCode, String state) {
        try {
            String accessToken = exchangeCodeForAccessToken(authorizationCode);
            JsonNode body = restClient.get()
                    .uri(USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            return new SocialUserInfo(
                    supports(),
                    getRequiredText(body, "sub"),
                    getOptionalText(body, "email")
            );
        } catch (RestClientException e) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    @Override
    public SocialUserInfo getUserInfoByIdToken(String idToken) {
        try {
            GoogleIdToken verifiedIdToken = idTokenVerifier.verify(idToken);
            if (verifiedIdToken == null) {
                throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
            }

            GoogleIdToken.Payload payload = verifiedIdToken.getPayload();
            return new SocialUserInfo(
                    supports(),
                    payload.getSubject(),
                    payload.getEmail()
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    /**
     * Google token endpoint와 통신해 authorization code를 access token으로 교환합니다.
     */
    private String exchangeCodeForAccessToken(String authorizationCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        JsonNode body = restClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(JsonNode.class);

        return getRequiredText(body, "access_token");
    }

    private String getRequiredText(JsonNode body, String fieldName) {
        if (body == null || body.path(fieldName).asText().isBlank()) {
            throw new GeneralException(AuthErrorCode.SOCIAL_USER_INFO_NOT_FOUND);
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
