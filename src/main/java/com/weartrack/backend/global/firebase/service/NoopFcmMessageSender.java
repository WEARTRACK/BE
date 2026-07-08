package com.weartrack.backend.global.firebase.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "firebase",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoopFcmMessageSender implements FcmMessageSender {

    @Override
    public void sendToTopic(
            String topic,
            String title,
            String body,
            Map<String, String> data
    ) {
        log.info("FCM is disabled. Topic message was not sent. topic={}", topic);
    }

    @Override
    public void sendToToken(
            String token,
            String title,
            String body,
            Map<String, String> data
    ) {
        log.info("FCM is disabled. Token message was not sent.");
    }
}
