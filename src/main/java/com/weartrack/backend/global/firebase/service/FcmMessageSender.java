package com.weartrack.backend.global.firebase.service;

import java.util.Map;

public interface FcmMessageSender {

    void sendToTopic(
            String topic,
            String title,
            String body,
            Map<String, String> data
    );
}
