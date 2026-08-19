package com.architek.oikos.notification.infrastructure.push;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.notification.application.port.out.PushNotificationPort;

/**
 * Real Expo Push API adapter (https://exp.host/--/api/v2/push/send).
 * Disabled by default (oikos.push.enabled=false, unlike
 * SmtpEmailAdapter/LoggingEmailAdapter which default the real adapter on):
 * mail has a working SMTP setup assumed in production, push does not yet -
 * no EAS project with push credentials is configured (see PLAN.md). Batches
 * at 100 tokens per request per the Expo API's own limit.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "oikos.push", name = "enabled", havingValue = "true")
public class ExpoPushAdapter implements PushNotificationPort {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final int MAX_TOKENS_PER_REQUEST = 100;

    private final RestClient restClient = RestClient.create();

    @Override
    public void sendPush(List<String> expoPushTokens, String title, String body, String linkPath) {
        for (int start = 0; start < expoPushTokens.size(); start += MAX_TOKENS_PER_REQUEST) {
            List<String> batch = expoPushTokens.subList(start, Math.min(start + MAX_TOKENS_PER_REQUEST, expoPushTokens.size()));
            sendBatch(batch, title, body, linkPath);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body, String linkPath) {
        try {
            List<Map<String, Object>> messages = tokens.stream()
                    .map(token -> Map.<String, Object>of(
                            "to", token,
                            "title", title,
                            "body", body == null ? "" : body,
                            "sound", "default",
                            "data", Map.of("linkPath", linkPath == null ? "" : linkPath)))
                    .toList();
            restClient.post().uri(EXPO_PUSH_URL).body(messages).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send Expo push to {} device(s)", tokens.size(), e);
        }
    }
}
