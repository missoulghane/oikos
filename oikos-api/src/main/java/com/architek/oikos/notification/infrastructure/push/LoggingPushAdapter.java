package com.architek.oikos.notification.infrastructure.push;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.notification.application.port.out.PushNotificationPort;

/**
 * No-op adapter used when oikos.push.enabled=false (the default - see
 * ExpoPushAdapter's javadoc): logs what would have been sent instead of
 * calling the Expo Push API, so notification-creating flows stay testable
 * without a configured EAS/Expo project.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "oikos.push", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingPushAdapter implements PushNotificationPort {

    @Override
    public void sendPush(List<String> expoPushTokens, String title, String body, String linkPath) {
        log.debug("Push sending disabled (oikos.push.enabled=false) - would have sent to {} device(s):\nTitle: {}\nBody: {}\nLink: {}",
                expoPushTokens.size(), title, body, linkPath);
    }
}
