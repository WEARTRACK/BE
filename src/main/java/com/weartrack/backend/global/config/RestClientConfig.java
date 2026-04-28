package com.weartrack.backend.global.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 외부 API 호출에 사용할 RestClient 빈을 등록한다.
 */
@Configuration
public class RestClientConfig {

    private final Duration connectTimeout;
    private final Duration readTimeout;

    public RestClientConfig(
            @Value("${oauth.client.connect-timeout-seconds:3}") long connectTimeoutSeconds,
            @Value("${oauth.client.read-timeout-seconds:5}") long readTimeoutSeconds
    ) {
        this.connectTimeout = Duration.ofSeconds(connectTimeoutSeconds);
        this.readTimeout = Duration.ofSeconds(readTimeoutSeconds);
    }

    /**
     * OAuth 외부 연동에 사용할 RestClient를 생성한다.
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .requestFactory(requestFactory)
                .build();
    }
}
