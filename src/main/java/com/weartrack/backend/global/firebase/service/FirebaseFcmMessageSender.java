package com.weartrack.backend.global.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseFcmMessageSender implements FcmMessageSender {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendToTopic(
            String topic,
            String title,
            String body,
            Map<String, String> data
    ) {
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("FCM 토픽 메시지 전송되었습니다. topic={}, messageId={}", topic, messageId);
        } catch (FirebaseMessagingException e) {
            log.error(
                    "FCM 토픽 메시지 전송에 실패했습니다. topic={}, errorCode={}",
                    topic,
                    e.getMessagingErrorCode(),
                    e
            );
        }
    }
}
