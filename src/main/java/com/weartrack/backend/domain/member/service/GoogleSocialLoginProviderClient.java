package com.weartrack.backend.domain.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleSocialLoginProviderClient implements SocialLoginProviderClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleSocialLoginProviderClient(
            RestClient restClient,
            @Value("${GOOGLE_CLIENT_ID}") String clientId,
            @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret,
            @Value("${GOOGLE_REDIRECT_URI}") String redirectUri
    ) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.GOOGLE;
    }

    @Override
    /**
     * Google 인가 코드를 access token으로 교환한 뒤 사용자 정보를 조회합니다.
     */
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
