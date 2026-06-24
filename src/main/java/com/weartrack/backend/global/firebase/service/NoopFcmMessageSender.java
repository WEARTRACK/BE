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
        log.info("FCM이 비활성화되어 있습니다. 토픽 메시지가 전송되지 않았습니다. topic={}", topic);
    }
}
