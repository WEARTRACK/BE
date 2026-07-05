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
            log.info("FCM topic message sent. topic={}, messageId={}", topic, messageId);
        } catch (FirebaseMessagingException e) {
            log.error(
                    "FCM topic message failed. topic={}, errorCode={}",
                    topic,
                    e.getMessagingErrorCode(),
                    e
            );
        }
    }

    @Override
    public void sendToToken(
            String token,
            String title,
            String body,
            Map<String, String> data
    ) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String messageId = firebaseMessaging.send(message);
            log.info("FCM token message sent. messageId={}", messageId);
        } catch (FirebaseMessagingException e) {
            log.error(
                    "FCM token message failed. errorCode={}",
                    e.getMessagingErrorCode(),
                    e
            );
        } catch (IllegalArgumentException e) {
            log.error("FCM token message could not be built.", e);
        }
    }
}
