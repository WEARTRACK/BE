package com.weartrack.backend.global.firebase.service;

import java.util.Map;

public interface FcmMessageSender {

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
        tokens.forEach(token -> sendToToken(token, title, body, data));
    }
}
