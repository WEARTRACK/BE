package com.weartrack.backend.global.firebase.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface FcmMessageSender {

    Logger LOG = LoggerFactory.getLogger(FcmMessageSender.class);

    void sendToTopic(
            String topic,
            String title,
            String body,
            Map<String, String> data
    );

    void sendToToken(
            String token,
            String title,
            String body,
            Map<String, String> data
    );

    default void sendToTokens(
            Iterable<String> tokens,
            String title,
            String body,
            Map<String, String> data
    ) {
        tokens.forEach(token -> {
            try {
                sendToToken(token, title, body, data);
            } catch (Exception e) {
                LOG.error("FCM 토큰 메시지 처리 중 예외가 발생했습니다.", e);
            }
        });
    }
}
